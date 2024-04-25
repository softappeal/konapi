@file:Suppress("SpellCheckingInspection", "unused")

package ch.softappeal.konapi.devices.hitachi

import ch.softappeal.konapi.Closeable
import ch.softappeal.konapi.I2cDevice
import ch.softappeal.konapi.sleepMs

/*
    Dot Matrix Liquid Crystal Display Controller/Driver

    The https://www.sparkfun.com/datasheets/LCD/HD44780.pdf is connected
    to an 8-bit I/O expander for I2C-bus (like https://www.nxp.com/docs/en/data-sheet/PCF8574_PCF8574A.pdf).
    Only the bus lines DB4 to DB7 are connected, DB0 to DB3 are unconnected.
    Therefore, the HD44780 must be in 4-bit interface mode, and it's not possible read data (busy flag, counters).
    The control pins are connected as below:
 */
private const val RS_DATA: UByte = 0x01U        // register select: data register
private const val RS_INSTRUCTION: UByte = 0x00U // register select: instruction register
private const val RW_READ: UByte = 0x02U
private const val RW_WRITE: UByte = 0x00U
private const val E_TRUE: UByte = 0x04U  // enable high
private const val E_FALSE: UByte = 0x00U // enable low
private const val BACKLIGHT_ON: UByte = 0x08U
private const val BACKLIGHT_OFF: UByte = 0x00U
private const val DB4_DB7: UByte = 0xF0U

// instructions:
private const val CLEAR_DISPLAY: UByte = 0x01U
private const val ENTRY_MODE_SET: UByte = 0x04U
private const val DISPLAY_CONTROL: UByte = 0x08U
private const val FUNCTION_SET: UByte = 0x20U
private const val SET_DDRAM_ADDR: UByte = 0x80U

// ENTRY_MODE_SET:
private const val INCREMENT: UByte = 0x02U
private const val DECREMENT: UByte = 0x00U
private const val SHIFT_ON: UByte = 0x01U
private const val SHIFT_OFF: UByte = 0x00U

// DISPLAY_CONTROL:
private const val DISPLAY_ON: UByte = 0x04U
private const val DISPLAY_OFF: UByte = 0x00U
private const val CURSOR_ON: UByte = 0x02U
private const val CURSOR_OFF: UByte = 0x00U
private const val BLINK_ON: UByte = 0x01U
private const val BLINK_OFF: UByte = 0x00U

// FUNCTION_SET:
private const val MODE_8BIT: UByte = 0x10U
private const val MODE_4BIT: UByte = 0x00U
private const val LINES_2: UByte = 0x08U
private const val LINES_1: UByte = 0x00U
private const val FONT_5x10: UByte = 0x04U
private const val FONT_5x8: UByte = 0x00U

public class I2cHd44780(private val device: I2cDevice, public val config: Config) : Closeable {
    public enum class Font { Dots5x8, Dots5x10 }

    public data class Config(
        public val lines: Int,
        public val columns: Int,
        public val font: Font,
    ) {
        init {
            require(lines == 1 || lines == 2 || lines == 4) { "lines must be 1, 2 or 4" }
            require(columns > 0) { "colums must be > 0" }
        }

        // https://web.alfredstate.edu/faculty/weimandn/lcd/lcd_addressing/lcd_addressing_index.html
        internal fun lineOffset(line: Int) = when (line) {
            0 -> 0x00
            1 -> 0x40
            2 -> columns
            3 -> 0x40 + columns
            else -> error("invalid line")
        }

        internal fun requireCursorPosition(line: Int, column: Int) {
            require(line in 0..<lines) { "line must be in 0..<$lines" }
            require(column in 0..<columns) { "column must be in 0..<$columns" }
        }
    }

    private var blink = false
    private var cursor = false
    private var on = true
    private var backlight = true

    private fun writeUpper(upperData: UByte, dataRegister: Boolean = false) {
        val data = RW_WRITE or
            (upperData and DB4_DB7) or
            (if (dataRegister) RS_DATA else RS_INSTRUCTION) or
            (if (backlight) BACKLIGHT_ON else BACKLIGHT_OFF)
        device.write(data or E_FALSE)
        device.write(data or E_TRUE)
        device.write(data or E_FALSE)
    }

    private fun write4Bit(instruction: UByte, dataRegister: Boolean = false) {
        writeUpper(instruction, dataRegister)
        writeUpper((instruction.toInt() shl 4).toUByte(), dataRegister)
    }

    private fun setDisplayControl() {
        write4Bit(
            DISPLAY_CONTROL or
                (if (on) DISPLAY_ON else DISPLAY_OFF) or
                (if (cursor) CURSOR_ON else CURSOR_OFF) or
                (if (blink) BLINK_ON else BLINK_OFF)
        )
    }

    init {
        sleepMs(100) // power on reset

        /*
            The LCD may initially be in one of three states.
            The following algorithm ensures that the LCD is in the 8-bit mode.

            Starting in state 1 (8-bit configuration):
                Send Function Set command. Command will be executed, set 8-bit mode.
                Send Function Set command. Command will be executed, set 8-bit mode.
                Send Function Set command. Command will be executed, set 8-bit mode.

            Starting in state 2 (4-bit configuration, waiting for first 4-bit transfer):
                Send Function Set command. First 4 bits received.
                Send Function Set command. Last 4 bits, command accepted, set 8-bit mode.
                Send Function Set command. Command will be executed, set 8-bit mode.

            Starting in state 3 (4-bit configuration, waiting for last 4-bit transfer):
                Send Function Set command. Last 4 bits, unknown command executed.
                Send Function Set command. In 8-bit mode command will be executed, otherwise first 4 bits received.
                Send Function Set command. 8-bit command will be executed or last 4 bits of previous command; set 8-bit mode.
         */
        writeUpper(FUNCTION_SET or MODE_8BIT)
        sleepMs(2) // needed for state 3: max. delay for unknown command
        writeUpper(FUNCTION_SET or MODE_8BIT)
        writeUpper(FUNCTION_SET or MODE_8BIT) // we are now guaranteed in 8-bit mode
        writeUpper(FUNCTION_SET or MODE_4BIT) // we are now guaranteed in 4-bit mode

        write4Bit(
            FUNCTION_SET or
                MODE_4BIT or
                when (config.lines) {
                    1 -> LINES_1
                    else -> LINES_2
                } or
                when (config.font) {
                    Font.Dots5x8 -> FONT_5x8
                    Font.Dots5x10 -> FONT_5x10
                }
        )
        write4Bit(ENTRY_MODE_SET or INCREMENT or SHIFT_OFF)
        setDisplayControl()
        clear()
    }

    public fun clear() {
        write4Bit(CLEAR_DISPLAY)
        sleepMs(2)
    }

    public fun displayString(s: String) {
        s.forEach { char -> write4Bit(char.code.toUByte(), dataRegister = true) }
    }

    public fun setCursorPosition(line: Int, column: Int) {
        config.requireCursorPosition(line, column)
        write4Bit(SET_DDRAM_ADDR or (config.lineOffset(line) + column).toUByte())
    }

    public fun setBlink(value: Boolean) {
        if (blink == value) return
        blink = value
        setDisplayControl()
    }

    public fun showCursor(value: Boolean) {
        if (cursor == value) return
        cursor = value
        setDisplayControl()
    }

    public fun showDisplay(value: Boolean) {
        if (on == value) return
        on = value
        setDisplayControl()
    }

    public fun setBacklight(value: Boolean) {
        if (backlight == value) return
        backlight = value
        setDisplayControl()
    }

    override fun close() {
        backlight = false
        on = false
        setDisplayControl()
    }
}

public fun i2cLcd1602(i2cDevice: I2cDevice): I2cHd44780 = I2cHd44780(i2cDevice, I2cHd44780.Config(2, 16, I2cHd44780.Font.Dots5x8))
