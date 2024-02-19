@file:Suppress("SpellCheckingInspection")

package ch.softappeal.kopi.lib.i2c

import ch.softappeal.kopi.lib.SuspendCloseable
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/*
  https://en.wikipedia.org/wiki/Hitachi_HD44780_LCD_controller
 */

private const val LCD_CLEARDISPLAY: UByte = 0x01U
private const val LCD_ENTRYMODESET: UByte = 0x04U
private const val LCD_DISPLAYCONTROL: UByte = 0x08U
private const val LCD_FUNCTIONSET: UByte = 0x20U
private const val LCD_SETDDRAMADDR: UByte = 0x80U

private const val LCD_ENTRYLEFT: UByte = 0x02U

private const val LCD_DISPLAYON: UByte = 0x04U
private const val LCD_DISPLAYOFF: UByte = 0x00U
private const val LCD_CURSORON: UByte = 0x02U
private const val LCD_CURSOROFF: UByte = 0x00U
private const val LCD_BLINKON: UByte = 0x01U
private const val LCD_BLINKOFF: UByte = 0x00U

private const val LCD_8BITMODE: UByte = 0x10U
private const val LCD_4BITMODE: UByte = 0x00U
private const val LCD_2LINE: UByte = 0x08U
private const val LCD_1LINE: UByte = 0x00U
private const val LCD_5x10DOTS: UByte = 0x04U
private const val LCD_5x8DOTS: UByte = 0x00U

private const val LCD_BACKLIGHT: UByte = 0x08U
private const val LCD_NOBACKLIGHT: UByte = 0x00U

private const val En: UByte = 0b00000100U
private const val Rw: UByte = 0b00000010U
private const val Rs: UByte = 0b00000001U

public suspend fun hd44780(device: I2cDevice, config: Hd44780.Config): Hd44780 = Hd44780(device, config).apply { init() }

public class Hd44780 internal constructor(private val device: I2cDevice, public val config: Config) : SuspendCloseable {
    public enum class Font { Dots5x8, Dots5x10 }

    public class Config(public val lines: Int, public val columns: Int, public val font: Font)

    internal suspend fun init() {
        write(0x03U)
        write(0x03U)
        write(0x03U)
        write(0x02U)
        write(
            LCD_FUNCTIONSET or
                LCD_4BITMODE or
                when (config.lines) {
                    1 -> LCD_1LINE
                    else -> LCD_2LINE
                } or
                when (config.font) {
                    Font.Dots5x8 -> LCD_5x8DOTS
                    Font.Dots5x10 -> LCD_5x10DOTS
                }
        )
        setDisplayControl()
        write(LCD_CLEARDISPLAY)
        write(LCD_ENTRYMODESET or LCD_ENTRYLEFT)
        delay(200.milliseconds)
    }

    private suspend fun setDisplayControl() {
        write(
            LCD_DISPLAYCONTROL or
                (if (on) LCD_DISPLAYON else LCD_DISPLAYOFF) or
                (if (cursor) LCD_CURSORON else LCD_CURSOROFF) or
                (if (blink) LCD_BLINKON else LCD_BLINKOFF)
        )
    }

    private suspend fun write(cmd: UByte, mode: UByte = 0x00U) {
        suspend fun write4(data: UByte) {
            val backlight = if (backlight) LCD_BACKLIGHT else LCD_NOBACKLIGHT
            device.write(data or backlight)
            device.write(data or En or backlight)
            delay(5.milliseconds)
            device.write((data and En.inv()) or backlight)
            delay(1.milliseconds)
        }
        write4(mode or (cmd and 0xF0U))
        write4(mode or ((cmd.toInt() shl 4).toUByte() and 0xF0U))
    }

    public suspend fun clear() {
        write(LCD_CLEARDISPLAY)
        delay(2.milliseconds)
    }

    public suspend fun displayString(s: String) {
        s.forEach { char -> write(char.code.toUByte(), Rs) }
    }

    public suspend fun setCursorPosition(line: Int, column: Int) {
        val rowOffset = when (line) {
            0 -> 0x00
            1 -> 0x40
            2 -> 0x14
            3 -> 0x54
            else -> error("Displays with more than 4 lines are not supported")
        }
        write(LCD_SETDDRAMADDR or (column + rowOffset).toUByte())
    }

    override suspend fun close() {
        backlight = false
        on = false
        setDisplayControl()
    }

    private var blink: Boolean = false
    public suspend fun setCursorBlink(value: Boolean) {
        if (blink == value) return
        blink = value
        setDisplayControl()
    }

    private var cursor: Boolean = false
    public suspend fun showCursor(value: Boolean) {
        if (cursor == value) return
        cursor = value
        setDisplayControl()
    }

    private var backlight: Boolean = true
    public suspend fun setBacklight(value: Boolean) {
        if (backlight == value) return
        backlight = value
        setDisplayControl()
    }

    private var on: Boolean = true
    public suspend fun showDisplay(value: Boolean) {
        if (on == value) return
        on = value
        setDisplayControl()
    }
}

public suspend fun lcd1602(i2cDevice: I2cDevice): Hd44780 = hd44780(i2cDevice, Hd44780.Config(2, 16, Hd44780.Font.Dots5x8))
