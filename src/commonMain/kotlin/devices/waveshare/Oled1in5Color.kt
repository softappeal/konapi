@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.devices.waveshare

import ch.softappeal.kopi.Closeable
import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.SPI_MODE_3
import ch.softappeal.kopi.SPI_MODE_4WIRE
import ch.softappeal.kopi.SPI_MODE_LSB_LAST
import ch.softappeal.kopi.SpiDevice
import ch.softappeal.kopi.graphics.Display
import ch.softappeal.kopi.graphics.Graphics
import ch.softappeal.kopi.graphics.GraphicsColor
import ch.softappeal.kopi.tryCatch
import ch.softappeal.kopi.tryFinally
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/*
    1.5inch RGB OLED Module

    https://www.waveshare.com/wiki/1.5inch_RGB_OLED_Module
    https://files.waveshare.com/upload/a/a7/SSD1351-Revision_1.5.pdf
    https://files.waveshare.com/upload/2/2c/OLED_Module_Code.7z
 */

private const val WIDTH = 128
private const val HEIGHT = 128

public interface Oled1in5Color : Closeable {
    public val graphics: Graphics
}

public suspend fun Oled1in5Color(spiDevice: SpiDevice, gpio: Gpio, dcPin: Int, rstPin: Int): Oled1in5Color {
    spiDevice.config = SpiDevice.Config(4_000_000U, 8U, SPI_MODE_3 or SPI_MODE_4WIRE or SPI_MODE_LSB_LAST)
    val dc = gpio.output(dcPin, false)
    val rst = tryCatch({
        gpio.output(rstPin, false)
    }) {
        dc.close()
    }

    fun closeOutputs() {
        tryFinally({
            dc.close()
        }) {
            rst.close()
        }
    }

    suspend fun init(): Oled1in5Color {
        rst.set(true)
        delay(100.milliseconds)
        rst.set(false)
        delay(100.milliseconds)
        rst.set(true)
        delay(100.milliseconds)

        val oneByte = UByteArray(1)

        fun writeByte(byte: UByte) {
            oneByte[0] = byte
            spiDevice.write(oneByte)
        }

        fun command(cmd: UByte) {
            dc.set(false)
            writeByte(cmd)
        }

        fun data(data: UByte) {
            dc.set(true)
            writeByte(data)
        }

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

        delay(100.milliseconds)
        command(0xAFU) // turn on oled panel

        val graphics = GraphicsColor(object : Display {
            override val width = WIDTH
            override val height = HEIGHT

            override suspend fun update(buffer: UByteArray) {
                command(0x15U) // set column address
                data(0x00U) // column address start 00
                data(0x7FU) // column address end 127
                command(0x75U) // set row address
                data(0x00U) // row address start 00
                data(0x7FU) // row address end 127
                command(0x5CU)
                dc.set(true)
                spiDevice.write(buffer)
            }
        })

        return object : Oled1in5Color {
            override val graphics = graphics
            override fun close() {
                closeOutputs()
            }
        }
    }

    return tryCatch({
        init()
    }) {
        closeOutputs()
    }
}
