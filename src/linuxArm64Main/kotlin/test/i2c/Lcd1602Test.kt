package ch.softappeal.kopi.test.i2c

import ch.softappeal.kopi.lib.i2c.I2c
import ch.softappeal.kopi.lib.i2c.lcd1602
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

public suspend fun lcd1602Test() {
    I2c(1).use { i2c ->
        lcd1602(i2c.device(0x027)).use { lcd ->
            delay(1.seconds)
            lcd.setBacklight(false)
            delay(1.seconds)
            lcd.setBacklight(true)
            lcd.showCursor(true)
            lcd.setBlink(true)
            lcd.setCursorPosition(1, 3)
            delay(2.seconds)
            lcd.setBlink(false)
            delay(2.seconds)
            lcd.showCursor(false)
            delay(2.seconds)
            lcd.clear()
            lcd.setCursorPosition(0, 0)
            lcd.displayString("123456789012345")
            lcd.setCursorPosition(1, lcd.config.columns - 15)
            lcd.displayString("234567890123456")
            delay(2.seconds)
            lcd.showDisplay(false)
            delay(2.seconds)
            lcd.showDisplay(true)
            delay(2.seconds)
        }
    }
}
