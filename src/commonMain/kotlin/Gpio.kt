package ch.softappeal.konapi

// https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#gpio

public interface Gpio : Closeable {
    public enum class Bias { Disable, PullDown, PullUp }
    public enum class Active { Low, High }

    public interface Output : Closeable {
        public fun set(value: Boolean)
    }

    public interface Input : Closeable {
        public fun get(): Boolean
    }

    public fun output(line: Int, initValue: Boolean, active: Active = Active.High): Output

    public fun input(line: Int, bias: Bias, active: Active = Active.High): Input
}

public expect fun Gpio(path: String): Gpio
