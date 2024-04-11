package ch.softappeal.kopi

import kotlin.time.Duration

// https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#gpio-and-the-40-pin-header

public typealias GpioNotification = (edge: Gpio.Edge, nanoSeconds: Long) -> Boolean

/**
 * NOTE: [close] also closes all open inputs/outputs.
 */
public interface Gpio : Closeable {
    public enum class Bias { Disable, PullDown, PullUp }
    public enum class Active { Low, High }
    public enum class Edge { Rising, Falling }

    public interface Output : Closeable {
        public fun set(value: Boolean)
    }

    public interface Input : Closeable {
        public fun get(): Boolean
    }

    public fun output(
        line: Int,
        initValue: Boolean,
        active: Active = Active.High,
    ): Output

    public fun input(
        line: Int,
        bias: Bias,
        active: Active = Active.High,
    ): Input

    /**
     * Returns if [GpioNotification] returns false or if [timeout] reached.
     * @return false if [timeout] reached else true
     */
    public fun listen(
        line: Int,
        bias: Bias,
        timeout: Duration,
        active: Active = Active.High,
        notification: GpioNotification,
    ): Boolean
}

public expect fun Gpio(label: String): Gpio

@Suppress("SpellCheckingInspection")
public fun Gpio(): Gpio = try {
    Gpio("pinctrl-rp1")
} catch (ignored: Exception) {
    Gpio("pinctrl-bcm2835")
}

public object DummyGpio : Gpio {
    override fun close(): Unit = Unit
    override fun output(line: Int, initValue: Boolean, active: Gpio.Active): Gpio.Output = throw NotImplementedError()
    override fun input(line: Int, bias: Gpio.Bias, active: Gpio.Active): Gpio.Input = throw NotImplementedError()
    override fun listen(
        line: Int, bias: Gpio.Bias, timeout: Duration, active: Gpio.Active, notification: GpioNotification,
    ): Boolean = true
}
