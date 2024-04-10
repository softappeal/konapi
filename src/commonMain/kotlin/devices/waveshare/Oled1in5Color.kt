@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.devices.waveshare

import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.SpiDevice
import ch.softappeal.kopi.graphics.Display
import ch.softappeal.kopi.graphics.GraphicsColor
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/*
    1.5inch RGB OLED Module

    https://www.waveshare.com/wiki/1.5inch_RGB_OLED_Module
    https://files.waveshare.com/upload/a/a7/SSD1351-Revision_1.5.pdf
    https://files.waveshare.com/upload/2/2c/OLED_Module_Code.7z
 */

public suspend fun oled1in5Color(spiDevice: SpiDevice, gpio: Gpio, dcPin: Int, rstPin: Int): Oled = Oled(
    null, spiDevice, gpio, dcPin, rstPin,
    {
        command(0xFDU) // command lock
        spiData(0x12U)
        command(0xFDU) // command lock
        spiData(0xB1U)
        command(0xAEU) // display off
        command(0xA4U) // Normal Display mode
        command(0x15U) // set column address
        spiData(0x00U) // column address start 00
        spiData(0x7FU) // column address end 127
        command(0x75U) // set row address
        spiData(0x00U) // row address start 00
        spiData(0x7FU) // row address end 127
        command(0xB3U)
        spiData(0xF1U)
        command(0xCAU)
        spiData(0x7FU)
        command(0xA0U) // set re-map & data format
        spiData(0x74U) // Horizontal address increment
        command(0xA1U) // set display start line
        spiData(0x00U) // start 00 line
        command(0xA2U) // set display offset
        spiData(0x00U)
        command(0xABU)
        command(0x01U)
        command(0xB4U)
        spiData(0xA0U)
        spiData(0xB5U)
        spiData(0x55U)
        command(0xC1U)
        spiData(0xC8U)
        spiData(0x80U)
        spiData(0xC0U)
        command(0xC7U)
        spiData(0x0FU)
        command(0xB1U)
        spiData(0x32U)
        command(0xB2U)
        spiData(0xA4U)
        spiData(0x00U)
        spiData(0x00U)
        command(0xBBU)
        spiData(0x17U)
        command(0xB6U)
        spiData(0x01U)
        command(0xBEU)
        spiData(0x05U)
        command(0xA6U)
        delay(100.milliseconds)
        command(0xAFU) // turn on oled panel
    },
    {
        GraphicsColor(object : Display {
            override val width = 128
            override val height = 128

            override suspend fun update(buffer: UByteArray) {
                command(0x15U) // set column address
                spiData(0x00U) // column address start 00
                spiData(0x7FU) // column address end 127
                command(0x75U) // set row address
                spiData(0x00U) // row address start 00
                spiData(0x7FU) // row address end 127
                command(0x5CU)
                dc!!.set(true)
                spiDevice.write(buffer)
            }
        })
    },
)
