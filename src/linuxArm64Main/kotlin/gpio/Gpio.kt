@file:OptIn(ExperimentalForeignApi::class)

package ch.softappeal.kopi.gpio

import ch.softappeal.kopi.Closeable
import ch.softappeal.kopi.gpio.native.GPIOD_LINE_EVENT_FALLING_EDGE
import ch.softappeal.kopi.gpio.native.GPIOD_LINE_EVENT_RISING_EDGE
import ch.softappeal.kopi.gpio.native.GPIOD_LINE_REQUEST_FLAG_ACTIVE_LOW
import ch.softappeal.kopi.gpio.native.GPIOD_LINE_REQUEST_FLAG_BIAS_DISABLE
import ch.softappeal.kopi.gpio.native.GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_DOWN
import ch.softappeal.kopi.gpio.native.GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_UP
import ch.softappeal.kopi.gpio.native.gpiod_chip_close
import ch.softappeal.kopi.gpio.native.gpiod_chip_get_line
import ch.softappeal.kopi.gpio.native.gpiod_chip_open_by_label
import ch.softappeal.kopi.gpio.native.gpiod_line_event
import ch.softappeal.kopi.gpio.native.gpiod_line_event_read
import ch.softappeal.kopi.gpio.native.gpiod_line_event_wait
import ch.softappeal.kopi.gpio.native.gpiod_line_get_value
import ch.softappeal.kopi.gpio.native.gpiod_line_release
import ch.softappeal.kopi.gpio.native.gpiod_line_request_both_edges_events_flags
import ch.softappeal.kopi.gpio.native.gpiod_line_request_input_flags
import ch.softappeal.kopi.gpio.native.gpiod_line_request_output_flags
import ch.softappeal.kopi.gpio.native.gpiod_line_set_value
import ch.softappeal.kopi.gpio.native.gpiod_version_string
import ch.softappeal.kopi.tryFinally
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toByte
import kotlinx.cinterop.toKString
import kotlin.time.Duration

// https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#gpio-and-the-40-pin-header

/**
 * gpiodetect -v
 *   gpiodetect (libgpiod) v1.6.3
 * curl -o src/nativeInterop/cinterop/headers/gpiod.h https://git.kernel.org/pub/scm/libs/libgpiod/libgpiod.git/plain/include/gpiod.h\?h=v1.6.3
 *
 * ldd /usr/bin/gpiodetect
 *   libgpiod.so.2 => /lib/aarch64-linux-gnu/libgpiod.so.2 (0x00007fff8a300000)
 * scp guru@raspberrypi:/lib/aarch64-linux-gnu/libgpiod.so.2 src/nativeInterop/cInterop/libs/libgpiod.so
 */
@Suppress("SpellCheckingInspection")
private const val EXPECTED_LIB_VERSION = "1.6.3"

@Suppress("SpellCheckingInspection")
public const val GPIO_RASPBERRY_PI_5: String = "pinctrl-rp1"

@Suppress("SpellCheckingInspection")
public const val GPIO_RASPBERRY_PI_ZERO_2: String = "pinctrl-bcm2835"

public fun findGpioLabel(): String {
    fun tryLabel(label: String) = label.apply { Gpio(this).close() }
    return try {
        tryLabel(GPIO_RASPBERRY_PI_5)
    } catch (ignored: Exception) {
        tryLabel(GPIO_RASPBERRY_PI_ZERO_2)
    }
}

public typealias GpioNotification = suspend (edge: Gpio.Edge, nanoSeconds: Long) -> Boolean

private inline fun Boolean.ordinal() = toByte().toInt()
private inline fun flags(active: Gpio.Active, bias: Gpio.Bias = Gpio.Bias.Disable) = active.value + bias.value

public open class Gpio(label: String, @Suppress("SpellCheckingInspection") private val consumer: String = "kopi") : Closeable {
    public enum class Bias(internal val value: Int) {
        Disable(GPIOD_LINE_REQUEST_FLAG_BIAS_DISABLE.toInt()),
        PullDown(GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_DOWN.toInt()),
        PullUp(GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_UP.toInt()),
    }

    public enum class Active(internal val value: Int) {
        Low(GPIOD_LINE_REQUEST_FLAG_ACTIVE_LOW.toInt()),
        High(0),
    }

    public enum class Edge { Rising, Falling }

    public interface Output : Closeable {
        public fun set(value: Boolean)
    }

    public interface Input : Closeable {
        public fun get(): Boolean
    }

    init {
        val actualLibVersion = gpiod_version_string()!!.toKString()
        check(EXPECTED_LIB_VERSION == actualLibVersion) {
            "lib version is '$actualLibVersion' but should be '$EXPECTED_LIB_VERSION'"
        }
    }

    private val chip = gpiod_chip_open_by_label(label) ?: error("no chip with label '$label'")

    public fun output(line: Int, initValue: Boolean, active: Active = Active.High): Output {
        val linePtr = getLine(line)
        check(gpiod_line_request_output_flags(linePtr, consumer, flags(active), initValue.ordinal()) == 0) {
            "can't request output for line $line"
        }
        return object : Output {
            override fun set(value: Boolean) {
                check(gpiod_line_set_value(linePtr, value.ordinal()) == 0) { "can't set value for line $line" }
            }

            override fun close() = gpiod_line_release(linePtr)
        }
    }

    public fun input(line: Int, bias: Bias, active: Active = Active.High): Input {
        val linePtr = getLine(line)
        check(gpiod_line_request_input_flags(linePtr, consumer, flags(active, bias)) == 0) {
            "can't request input for line $line"
        }
        return object : Input {
            override fun get() = when (gpiod_line_get_value(linePtr)) {
                0 -> false
                1 -> true
                else -> error("can't get value for line $line")
            }

            override fun close() = gpiod_line_release(linePtr)
        }
    }

    /**
     * Returns if [GpioNotification] returns false or if [timeout] reached.
     * @return false if [timeout] reached else true
     */
    public suspend fun listen(
        line: Int, bias: Bias, timeout: Duration, active: Active = Active.High, notification: GpioNotification,
    ): Boolean {
        val linePtr = getLine(line)
        check(gpiod_line_request_both_edges_events_flags(linePtr, consumer, flags(active, bias)) == 0) {
            "can't request events for line $line"
        }
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
                            check(gpiod_line_event_read(linePtr, event.ptr) == 0) { error("can't read event for line $line") }
                            val edge = when (event.event_type.toUInt()) {
                                GPIOD_LINE_EVENT_RISING_EDGE -> Edge.Rising
                                GPIOD_LINE_EVENT_FALLING_EDGE -> Edge.Falling
                                else -> error("unexpected event on line $line")
                            }
                            if (!notification(edge, ts.tv_sec * 1_000_000_000 + ts.tv_nsec)) break
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

    private fun getLine(line: Int) = gpiod_chip_get_line(chip, line.toUInt()) ?: error("can't get line $line")

    /** NOTE: Also closes all open inputs/outputs. */
    override fun close() {
        gpiod_chip_close(chip)
    }
}
