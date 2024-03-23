package ch.softappeal.kopi.ic2

import ch.softappeal.kopi.GPIO_IN_CONNECTED_TO_PAJ7620U2_INT
import ch.softappeal.kopi.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.kopi.I2C_BUS
import ch.softappeal.kopi.gpio.Gpio
import ch.softappeal.kopi.gpio.Gpio.Bias
import ch.softappeal.kopi.gpio.Gpio.Edge
import ch.softappeal.kopi.i2c.I2cBus
import ch.softappeal.kopi.i2c.paj7620U2
import ch.softappeal.kopi.use
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

abstract class Paj7620U2Test {
    @Test
    fun test() {
        runBlocking {
            I2cBus(I2C_BUS).use { i2c ->
                val paj7620U2 = paj7620U2(i2c.device(I2C_ADDRESS_PAJ7620U2))
                println(paj7620U2.gesture())
                launch {
                    Gpio().use { chip ->
                        chip.listen(GPIO_IN_CONNECTED_TO_PAJ7620U2_INT, Bias.PullUp, 10.seconds) { edge, _ ->
                            if (edge == Edge.Falling) println(paj7620U2.gesture())
                            true
                        }
                    }
                }
            }
        }
    }
}
