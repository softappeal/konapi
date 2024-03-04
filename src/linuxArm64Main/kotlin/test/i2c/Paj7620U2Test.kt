package ch.softappeal.kopi.test.i2c

import ch.softappeal.kopi.lib.gpio.Gpio
import ch.softappeal.kopi.lib.i2c.I2c
import ch.softappeal.kopi.lib.i2c.paj7620U2
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days

private const val IN = 26 // NOTE: connected to INT pin of Paj7620U2

public suspend fun paj7620U2Test(gpioLabel: String) {
    I2c(1).use { i2c ->
        val paj7620U2 = paj7620U2(i2c.device(0x73))
        println(paj7620U2.gesture())
        coroutineScope {
            launch(Dispatchers.Default) {
                Gpio(gpioLabel).use { chip ->
                    chip.listen(IN, Gpio.Bias.PullUp, 100.days) { edge, _ ->
                        if (edge == Gpio.Edge.Falling) println(paj7620U2.gesture()?.name ?: "<none>")
                        true
                    }
                }
            }
        }
    }
}
