package ch.softappeal.kopi.lib

import ch.softappeal.kopi.gpiod.GPIOD_LINE_EVENT_FALLING_EDGE
import ch.softappeal.kopi.gpiod.GPIOD_LINE_EVENT_RISING_EDGE
import ch.softappeal.kopi.gpiod.GPIOD_LINE_REQUEST_FLAG_ACTIVE_LOW
import ch.softappeal.kopi.gpiod.GPIOD_LINE_REQUEST_FLAG_BIAS_DISABLE
import ch.softappeal.kopi.gpiod.GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_DOWN
import ch.softappeal.kopi.gpiod.GPIOD_LINE_REQUEST_FLAG_BIAS_PULL_UP
import ch.softappeal.kopi.gpiod.gpiod_chip_close
import ch.softappeal.kopi.gpiod.gpiod_chip_get_line
import ch.softappeal.kopi.gpiod.gpiod_chip_open_by_label
import ch.softappeal.kopi.gpiod.gpiod_line_event
import ch.softappeal.kopi.gpiod.gpiod_line_event_read
import ch.softappeal.kopi.gpiod.gpiod_line_get_value
import ch.softappeal.kopi.gpiod.gpiod_line_release
import ch.softappeal.kopi.gpiod.gpiod_line_request_both_edges_events_flags
import ch.softappeal.kopi.gpiod.gpiod_line_request_input_flags
import ch.softappeal.kopi.gpiod.gpiod_line_request_output_flags
import ch.softappeal.kopi.gpiod.gpiod_line_set_value
import ch.softappeal.kopi.gpiod.gpiod_version_string
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toByte
import kotlinx.cinterop.toKString

// https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#gpio-and-the-40-pin-header

/**
 * ldd /usr/bin/gpiodetect
 *   libgpiod.so.2 => /lib/aarch64-linux-gnu/libgpiod.so.2 (0x00007fff8a300000)
 *
 * scp guru@raspberrypi:/lib/aarch64-linux-gnu/libgpiod.so.2 src/nativeInterop/cinterop/libgpiod.so
 *
 * curl -o src/nativeInterop/cinterop/headers/include/gpiod.h https://git.kernel.org/pub/scm/libs/libgpiod/libgpiod.git/plain/include/gpiod.h\?h=v1.6.3
 */
private const val EXPECTED_LIB_VERSION = "1.6.3"

private const val RASPBERRY_PI_5 = "pinctrl-rp1"

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

public typealias Input = () -> Boolean
public typealias Output = (value: Boolean) -> Unit
public typealias Notification = (edge: Edge, nanoSeconds: Long) -> Boolean

public open class Chip(label: String = RASPBERRY_PI_5, private val consumer: String = "<none>") : Closeable {
    init {
        val actualLibVersion = gpiod_version_string()!!.toKString()
        check(EXPECTED_LIB_VERSION == actualLibVersion) {
            "lib version is '$actualLibVersion' but should be '$EXPECTED_LIB_VERSION'"
        }
    }

    private val chip = gpiod_chip_open_by_label(label) ?: error("no chip with label '$label'")

    public fun output(line: Int, initValue: Boolean, active: Active = Active.High): Output {
        val linePtr = getLine(line)
        check(gpiod_line_request_output_flags(linePtr, consumer, active.value, initValue.toByte().toInt()) == 0) {
            "can't request output for line $line"
        }
        return { value ->
            check(gpiod_line_set_value(linePtr, value.toByte().toInt()) == 0) { "can't set value for line $line" }
        }
    }

    public fun input(line: Int, bias: Bias, active: Active = Active.High): Input {
        val linePtr = getLine(line)
        check(gpiod_line_request_input_flags(linePtr, consumer, active.value + bias.value) == 0) {
            "can't request input for line $line"
        }
        return {
            when (gpiod_line_get_value(linePtr)) {
                0 -> false
                1 -> true
                else -> error("can't get value for line $line")
            }
        }
    }

    /** Returns if [Notification] returns false. */
    public fun listen(line: Int, bias: Bias, active: Active = Active.High, notification: Notification) {
        val linePtr = getLine(line)
        check(gpiod_line_request_both_edges_events_flags(linePtr, consumer, active.value + bias.value) == 0) {
            "can't request events for line $line"
        }
        tryFinally({
            memScoped {
                val event = alloc<gpiod_line_event>()
                while (true) {
                    check(gpiod_line_event_read(linePtr, event.ptr) == 0) { error("can't read event for line $line") }
                    val edge = when (event.event_type.toUInt()) {
                        GPIOD_LINE_EVENT_RISING_EDGE -> Edge.Rising
                        GPIOD_LINE_EVENT_FALLING_EDGE -> Edge.Falling
                        else -> error("unexpected event on line $line")
                    }
                    val ts = event.ts
                    if (!notification(edge, ts.tv_sec * 1_000_000_000 + ts.tv_nsec)) break
                }
            }
        }) {
            gpiod_line_release(linePtr)
        }
    }

    private fun getLine(line: Int) = gpiod_chip_get_line(chip, line.toUInt()) ?: error("can't get line $line")

    override fun close() {
        gpiod_chip_close(chip)
    }
}
