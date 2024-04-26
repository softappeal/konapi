package ch.softappeal.konapi.devices

import ch.softappeal.konapi.I2C_ADDRESS_LCD1602
import ch.softappeal.konapi.devices.hitachi.i2cLcd1602
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.sleepMs
import ch.softappeal.konapi.use
import kotlin.test.Test

abstract class I2cLcd1602Test {
    @Test
    fun test() {
        i2cBus1().use { bus ->
            i2cLcd1602(bus.device(I2C_ADDRESS_LCD1602)).use { lcd ->
                lcd.setBacklight(true)
                sleepMs(2000)
                lcd.setBacklight(false)
                sleepMs(2000)
                lcd.setBacklight(true)
                lcd.showCursor(true)
                lcd.setBlink(true)
                lcd.setCursorPosition(1, 3)
                sleepMs(2000)
                lcd.setBlink(false)
                sleepMs(2000)
                lcd.showCursor(false)
                sleepMs(2000)
                lcd.clear()
                lcd.setCursorPosition(0, 0)
                lcd.displayString("123456789012345")
                lcd.setCursorPosition(1, lcd.config.columns - 15)
                lcd.displayString("234567890123456")
                sleepMs(2000)
                lcd.showDisplay(false)
                sleepMs(2000)
                lcd.showDisplay(true)
                sleepMs(2000)
            }
        }
    }
}
