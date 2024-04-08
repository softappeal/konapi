package ch.softappeal.kopi.devices

import ch.softappeal.kopi.GPIO_PAJ7620U2_INT
import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.kopi.devices.waveshare.Paj7620U2
import ch.softappeal.kopi.i2cBus1
import ch.softappeal.kopi.use
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

abstract class Paj7620U2Test {
    @Test
    fun test() {
        runBlocking {
            i2cBus1().use { bus ->
                val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
                println("gesture: ${paj7620U2.gesture()}")
                println("make all 9 gestures ...")
                Gpio().use { gpio ->
                    gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, 5.seconds) { edge, _ ->
                        if (edge == Gpio.Edge.Falling) println(paj7620U2.gesture())
                        true
                    }
                }
            }
        }
    }
}
