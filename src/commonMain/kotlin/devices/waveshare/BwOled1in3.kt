@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("SpellCheckingInspection")

package ch.softappeal.konapi.devices.waveshare

import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.SpiDevice
import ch.softappeal.konapi.graphics.BwGraphics
import ch.softappeal.konapi.graphics.Display
import ch.softappeal.konapi.sleepMs

/*
    1.3inch OLED Monochrome

    https://www.waveshare.com/wiki/1.3inch_OLED_(B)
    https://files.waveshare.com/upload/7/76/1.3inch_OLED_UserManual.pdf
    https://files.waveshare.com/upload/0/08/SH1106_V2.3.pdf
    https://files.waveshare.com/upload/2/2c/OLED_Module_Code.7z
 */

public fun bwOled1in3(
    gpio: Gpio, dcPin: Int, rstPin: Int,
    device: SpiDevice, speedHz: Int = 10_000_000,
): Oled<BwGraphics> = Oled(
    gpio, dcPin, rstPin,
    device, speedHz,
    {
        command(0xAEU) // turn off oled panel
        command(0x02U) // set low column address
        command(0x10U) // set high column address
        command(0x40U) // set start line address  Set Mapping RAM Display Start Line (0x00~0x3F)
        command(0x81U) // set contrast control register
        command(0xA0U) // Set SEG/Column Mapping
        command(0xC0U) // Set COM/Row Scan Direction
        command(0xA6U) // set normal display
        command(0xA8U) // set multiplex ratio(1 to 64)
        command(0x3FU) // 1/64 duty
        command(0xD3U) // set display offset    Shift Mapping RAM Counter (0x00~0x3F)
        command(0x00U) // not offset
        command(0xd5U) // set display clock divide ratio/oscillator frequency
        command(0x80U) // set divide ratio, Set Clock as 100 Frames/Sec
        command(0xD9U) // set pre-charge period
        command(0xF1U) // Set Pre-Charge as 15 Clocks & Discharge as 1 Clock
        command(0xDAU) // set com pins hardware configuration
        command(0x12U)
        command(0xDBU) // set vcomh
        command(0x40U) // Set VCOM Deselect Level
        command(0x20U) // Set Page Addressing Mode (0x00/0x01/0x02)
        command(0x02U)
        command(0xA4U) // Disable Entire Display On (0xa4/0xa5)
        command(0xA6U) // Disable Inverse Display On (0xa6/a7)
        sleepMs(100)
        command(0xAFU) // turn on oled panel
    },
    {
        BwGraphics(object : Display(128, 64) {
            private val chunk = UByteArray(width)
            override fun update(buffer: UByteArray) {
                var pageStart = 0
                for (page in 0..<8) {
                    command((0xB0U + page.toUByte()).toUByte()) // set page address
                    command(0x02U) // set low column address
                    command(0x10U) // set high column address
                    val pageEnd = pageStart + width
                    buffer.copyInto(chunk, startIndex = pageStart, endIndex = pageEnd)
                    data(chunk)
                    pageStart = pageEnd
                }
            }
        })
    },
)
