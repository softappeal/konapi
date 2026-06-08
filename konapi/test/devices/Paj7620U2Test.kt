package ch.softappeal.konapi.devices

import ch.softappeal.konapi.GPIO_PAJ7620U2_INT
import ch.softappeal.konapi.GPIO_PATH
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.sleepMs
import ch.softappeal.konapi.use
import kotlin.test.Test

abstract class Paj7620U2Test {
    @Test
    fun test() {
        println("make all 9 gestures ...")
        i2cBus1().use { bus ->
            Gpio(GPIO_PATH).use { gpio ->
                gpio.input(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp).use { input ->
                    val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
                    var counter = 0
                    while (counter++ < 30) {
                        sleepMs(100)
                        if (!input.get()) {
                            println(paj7620U2.gesture())
                            counter = 0
                        }
                    }
                }
            }
        }
    }
}
