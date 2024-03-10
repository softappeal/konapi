package ch.softappeal.kopi.test.i2c

import ch.softappeal.kopi.lib.gpio.Gpio
import ch.softappeal.kopi.lib.i2c.Gesture
import ch.softappeal.kopi.lib.i2c.I2c
import ch.softappeal.kopi.lib.i2c.bme280
import ch.softappeal.kopi.lib.i2c.lcd1602
import ch.softappeal.kopi.lib.i2c.paj7620U2
import ch.softappeal.kopi.lib.use
import kotlinx.cinterop.CValuesRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import platform.posix.NULL
import platform.posix.time
import platform.posix.time_tVar
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days

private const val IN = 26 // NOTE: connected to INT pin of Paj7620U2

public suspend fun paj7620U2Test(gpioLabel: String) {
    I2c(1).use { i2c ->
        lcd1602(i2c.device(0x027)).use { lcd ->
            lcd.clear()
            lcd.setCursorPosition(0, 0)
            @Suppress("UNCHECKED_CAST") val time = time(NULL as CValuesRef<time_tVar>)
            lcd.displayString("Boot:$time")
            lcd.setCursorPosition(1, 0)
            lcd.displayString("REBOOTED")
            val paj7620U2 = paj7620U2(i2c.device(0x73))
            val bme280 = bme280(i2c.device(0x76))
            println(paj7620U2.gesture())
            coroutineScope {
                launch(Dispatchers.Default) {
                    Gpio(gpioLabel).use { chip ->
                        chip.listen(IN, Gpio.Bias.PullUp, 100.days) { edge, _ ->
                            if (edge == Gpio.Edge.Falling) {
                                val gesture = paj7620U2.gesture()
                                val measurements = bme280.measurements()
                                val string = when (gesture) {
                                    null -> "<none>"
                                    Gesture.Up -> "Temp:" + (measurements.temperaturInCelsius * 10).roundToInt() + " 0.1C"
                                    Gesture.Down -> "Press:" + (measurements.pressureInPascal / 100).roundToInt() + " hPa"
                                    Gesture.Left -> "Humid:" + (measurements.humidityInPercent * 10).roundToInt() + " 0.1%"
                                    else -> gesture.name
                                }
                                lcd.setCursorPosition(1, 0)
                                lcd.displayString(string + " ".repeat(lcd.config.columns - string.length))
                            }
                            true
                        }
                    }
                }
            }
        }
    }
}
