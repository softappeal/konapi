@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("SpellCheckingInspection")

package ch.softappeal.konapi

import ch.softappeal.konapi.native.gpio.GPIOD_LINE_EVENT_FALLING_EDGE
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_EVENT_RISING_EDGE
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_REQUEST_FLAG_ACTIVE_LOW
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_REQUEST_FLAG_BIAS_DISABLE
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_DOWN
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_UP
import ch.softappeal.konapi.native.gpio.gpiod_chip_close
import ch.softappeal.konapi.native.gpio.gpiod_chip_get_line
import ch.softappeal.konapi.native.gpio.gpiod_chip_open_by_label
import ch.softappeal.konapi.native.gpio.gpiod_line_event
import ch.softappeal.konapi.native.gpio.gpiod_line_event_read
import ch.softappeal.konapi.native.gpio.gpiod_line_event_wait
import ch.softappeal.konapi.native.gpio.gpiod_line_get_value
import ch.softappeal.konapi.native.gpio.gpiod_line_release
import ch.softappeal.konapi.native.gpio.gpiod_line_request_both_edges_events_flags
import ch.softappeal.konapi.native.gpio.gpiod_line_request_falling_edge_events_flags
import ch.softappeal.konapi.native.gpio.gpiod_line_request_input_flags
import ch.softappeal.konapi.native.gpio.gpiod_line_request_output_flags
import ch.softappeal.konapi.native.gpio.gpiod_line_request_rising_edge_events_flags
import ch.softappeal.konapi.native.gpio.gpiod_line_set_value
import ch.softappeal.konapi.native.gpio.gpiod_version_string
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toByte
import kotlinx.cinterop.toKString
import kotlin.time.Duration

/**
 * gpiodetect -v
 *   gpiodetect (libgpiod) v1.6.3
 * curl -o src/nativeInterop/cinterop/headers/gpiod.h https://git.kernel.org/pub/scm/libs/libgpiod/libgpiod.git/plain/include/gpiod.h\?h=v1.6.3
 *
 * ldd /usr/bin/gpiodetect
 *   libgpiod.so.2 => /lib/aarch64-linux-gnu/libgpiod.so.2 (0x00007fff8a300000)
 * scp me@pi5:/lib/aarch64-linux-gnu/libgpiod.so.2 src/nativeInterop/cinterop/libs/libgpiod.so
 */
private const val EXPECTED_LIB_VERSION = "1.6.3"

private const val CONSUMER = "konapi"

private fun Boolean.ordinal() = toByte().toInt()

private fun flags(active: Gpio.Active, bias: Gpio.Bias = Gpio.Bias.Disable) = when (active) {
    Gpio.Active.Low -> GPIOD_LINE_REQUEST_FLAG_ACTIVE_LOW.toInt()
    Gpio.Active.High -> 0
} +
    when (bias) {
        Gpio.Bias.Disable -> GPIOD_LINE_REQUEST_FLAG_BIAS_DISABLE
        Gpio.Bias.PullDown -> GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_DOWN
        Gpio.Bias.PullUp -> GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_UP
    }.toInt()

public actual fun Gpio(label: String): Gpio {
    val actualLibVersion = gpiod_version_string()!!.toKString()
    check(EXPECTED_LIB_VERSION == actualLibVersion) { "lib version is '$actualLibVersion' but should be '$EXPECTED_LIB_VERSION'" }
    val chip = gpiod_chip_open_by_label(label) ?: error("no chip with label '$label'")
    fun getLine(line: Int) = gpiod_chip_get_line(chip, line.convert()) ?: error("can't get line $line")
    return object : Gpio {
        override fun output(line: Int, initValue: Boolean, active: Gpio.Active): Gpio.Output {
            val linePtr = getLine(line)
            check(gpiod_line_request_output_flags(linePtr, CONSUMER, flags(active), initValue.ordinal()) == 0) {
                "can't request output for line $line"
            }
            return object : Gpio.Output {
                override fun set(value: Boolean) {
                    check(gpiod_line_set_value(linePtr, value.ordinal()) == 0) { "can't set value for line $line" }
                }

                override fun close() = gpiod_line_release(linePtr)
            }
        }

        override fun input(line: Int, bias: Gpio.Bias, active: Gpio.Active): Gpio.Input {
            val linePtr = getLine(line)
            check(gpiod_line_request_input_flags(linePtr, CONSUMER, flags(active, bias)) == 0) {
                "can't request input for line $line"
            }
            return object : Gpio.Input {
                override fun get() = when (gpiod_line_get_value(linePtr)) {
                    0 -> false
                    1 -> true
                    else -> error("can't get value for line $line")
                }

                override fun close() = gpiod_line_release(linePtr)
            }
        }

        override fun listen(
            line: Int, bias: Gpio.Bias, timeout: Duration, edge: Gpio.Edge, active: Gpio.Active, notification: GpioNotification,
        ): Boolean {
            require(timeout.isPositive()) { "timeout=$timeout must be positive" }
            val linePtr = getLine(line)
            val flags = flags(active, bias)
            check(when (edge) {
                Gpio.Edge.Rising -> gpiod_line_request_rising_edge_events_flags(linePtr, CONSUMER, flags)
                Gpio.Edge.Falling -> gpiod_line_request_falling_edge_events_flags(linePtr, CONSUMER, flags)
                Gpio.Edge.Both -> gpiod_line_request_both_edges_events_flags(linePtr, CONSUMER, flags)
            } == 0) { "can't request events for line $line" }
            tryFinally({
                memScoped {
                    val event = alloc<gpiod_line_event>()
                    val ts = event.ts
                    val inWholeNanoseconds = timeout.inWholeNanoseconds
                    val timeoutSeconds = inWholeNanoseconds / 1_000_000_000
                    val timeoutNanoseconds = inWholeNanoseconds % 1_000_000_000
                    while (true) {
                        ts.tv_sec = timeoutSeconds
                        ts.tv_nsec = timeoutNanoseconds
                        when (gpiod_line_event_wait(linePtr, ts.ptr)) {
                            0 -> return@listen false // timeout
                            1 -> { // there is an event
                                check(gpiod_line_event_read(linePtr, event.ptr) == 0) { "can't read event for line $line" }
                                val risingEdge = when (event.event_type.toUInt()) {
                                    GPIOD_LINE_EVENT_RISING_EDGE -> true
                                    GPIOD_LINE_EVENT_FALLING_EDGE -> false
                                    else -> error("unexpected event on line $line")
                                }
                                if (!notification(risingEdge, ts.tv_sec * 1_000_000_000 + ts.tv_nsec)) break
                            }
                            else -> error("can't wait for event on line $line")
                        }
                    }
                }
            }) {
                gpiod_line_release(linePtr)
            }
            return true
        }

        override fun close() {
            gpiod_chip_close(chip)
        }
    }
}
