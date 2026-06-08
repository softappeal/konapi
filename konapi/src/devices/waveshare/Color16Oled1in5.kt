@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.devices.waveshare

import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.SpiDevice
import ch.softappeal.konapi.graphics.Color16Graphics
import ch.softappeal.konapi.graphics.Display
import ch.softappeal.konapi.sleepMs

/*
    1.5inch RGB OLED Module

    https://www.waveshare.com/wiki/1.5inch_RGB_OLED_Module
    https://files.waveshare.com/upload/a/a7/SSD1351-Revision_1.5.pdf
    https://files.waveshare.com/upload/2/2c/OLED_Module_Code.7z
 */

public fun color16Oled1in5(
    gpio: Gpio, dcPin: Int, rstPin: Int,
    device: SpiDevice, speedHz: Int = 10_000_000,
): Oled<Color16Graphics> = Oled(
    gpio, dcPin, rstPin,
    device, speedHz,
    {
        command(0xFDU) // command lock
        data(0x12U)
        command(0xFDU) // command lock
        data(0xB1U)
        command(0xAEU) // display off
        command(0xA4U) // Normal Display mode
        command(0x15U) // set column address
        data(0x00U) // column address start 00
        data(0x7FU) // column address end 127
        command(0x75U) // set row address
        data(0x00U) // row address start 00
        data(0x7FU) // row address end 127
        command(0xB3U)
        data(0xF1U)
        command(0xCAU)
        data(0x7FU)
        command(0xA0U) // set re-map & data format
        data(0x74U) // Horizontal address increment
        command(0xA1U) // set display start line
        data(0x00U) // start 00 line
        command(0xA2U) // set display offset
        data(0x00U)
        command(0xABU)
        command(0x01U)
        command(0xB4U)
        data(0xA0U)
        data(0xB5U)
        data(0x55U)
        command(0xC1U)
        data(0xC8U)
        data(0x80U)
        data(0xC0U)
        command(0xC7U)
        data(0x0FU)
        command(0xB1U)
        data(0x32U)
        command(0xB2U)
        data(0xA4U)
        data(0x00U)
        data(0x00U)
        command(0xBBU)
        data(0x17U)
        command(0xB6U)
        data(0x01U)
        command(0xBEU)
        data(0x05U)
        command(0xA6U)
        sleepMs(100)
        command(0xAFU) // turn on oled panel
    },
    {
        Color16Graphics(object : Display(128, 128) {
            override fun update(buffer: UByteArray) {
                command(0x15U) // set column address
                data(0x00U) // column address start 00
                data(0x7FU) // column address end 127
                command(0x75U) // set row address
                data(0x00U) // row address start 00
                data(0x7FU) // row address end 127
                command(0x5CU)
                data(buffer)
            }
        })
    },
)
