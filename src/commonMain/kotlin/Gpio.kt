package ch.softappeal.konapi

import kotlin.time.Duration

// https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#gpio-and-the-40-pin-header

public typealias GpioNotification = (risingEdge: Boolean, nanoSeconds: Long) -> Boolean

/**
 * NOTE: [close] also closes all open inputs/outputs.
 */
public interface Gpio : Closeable {
    public enum class Bias { Disable, PullDown, PullUp }
    public enum class Active { Low, High }
    public enum class Edge { Rising, Falling, Both }

    public interface Output : Closeable {
        public fun set(value: Boolean)
    }

    public interface Input : Closeable {
        public fun get(): Boolean
    }

    public fun output(line: Int, initValue: Boolean, active: Active = Active.High): Output

    public fun input(line: Int, bias: Bias, active: Active = Active.High): Input

    /**
     * Returns if [notification] returns false or if [timeout] reached.
     * @return false if [timeout] reached else true
     */
    public fun listen(
        line: Int, bias: Bias, timeout: Duration, edge: Edge, active: Active = Active.High, notification: GpioNotification,
    ): Boolean
}

public expect fun Gpio(label: String): Gpio

@Suppress("SpellCheckingInspection")
public fun Gpio(): Gpio = try {
    Gpio("pinctrl-rp1")
} catch (ignored: Exception) {
    Gpio("pinctrl-bcm2835")
}
