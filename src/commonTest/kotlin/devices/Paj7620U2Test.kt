package ch.softappeal.konapi.devices

import ch.softappeal.konapi.GPIO_PAJ7620U2_INT
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.use
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

abstract class Paj7620U2Test {
    @Test
    fun test() {
        i2cBus1().use { bus ->
            Gpio().use { gpio ->
                println("make all 9 gestures ...")
                var counter = 15
                val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
                val timeout = !gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, 3.seconds, Gpio.Edge.Falling) { _, _ ->
                    println(paj7620U2.gesture())
                    counter-- > 0
                }
                println("timeout: $timeout")
            }
        }
    }
}
