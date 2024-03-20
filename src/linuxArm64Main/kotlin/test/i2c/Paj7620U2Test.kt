package ch.softappeal.kopi.test.i2c

import ch.softappeal.kopi.app.GPIO_IN_CONNECTED_TO_PAJ7620U2_INT
import ch.softappeal.kopi.app.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.kopi.app.I2C_BUS
import ch.softappeal.kopi.lib.gpio.Gpio
import ch.softappeal.kopi.lib.i2c.I2c
import ch.softappeal.kopi.lib.i2c.paj7620U2
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

public suspend fun paj7620U2Test(gpioLabel: String) {
    println("paj7620U2Test")
    I2c(I2C_BUS).use { i2c ->
        val paj7620U2 = paj7620U2(i2c.device(I2C_ADDRESS_PAJ7620U2))
        println(paj7620U2.gesture())
        coroutineScope {
            launch(Dispatchers.Default) {
                Gpio(gpioLabel).use { chip ->
                    chip.listen(GPIO_IN_CONNECTED_TO_PAJ7620U2_INT, Gpio.Bias.PullUp, 10.seconds) { edge, _ ->
                        if (edge == Gpio.Edge.Falling) println(paj7620U2.gesture())
                        true
                    }
                }
            }
        }
    }
}
