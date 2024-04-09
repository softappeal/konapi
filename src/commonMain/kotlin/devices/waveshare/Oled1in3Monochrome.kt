@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("SpellCheckingInspection")

package ch.softappeal.kopi.devices.waveshare

import ch.softappeal.kopi.Closeable
import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.SPI_MODE_3
import ch.softappeal.kopi.SPI_MODE_4WIRE
import ch.softappeal.kopi.SPI_MODE_LSB_LAST
import ch.softappeal.kopi.SpiDevice
import ch.softappeal.kopi.graphics.Display
import ch.softappeal.kopi.graphics.Graphics
import ch.softappeal.kopi.graphics.GraphicsMonochrome
import ch.softappeal.kopi.tryCatch
import ch.softappeal.kopi.tryFinally
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/*
    1.3inch OLED Monochrome

    https://www.waveshare.com/wiki/1.3inch_OLED_(B)
    https://files.waveshare.com/upload/7/76/1.3inch_OLED_UserManual.pdf
    https://files.waveshare.com/upload/0/08/SH1106_V2.3.pdf
    https://files.waveshare.com/upload/2/2c/OLED_Module_Code.7z
 */

private const val WIDTH = 128
private const val HEIGHT = 64

public interface Oled1in3Monochrome : Closeable {
    public val graphics: Graphics
}

public suspend fun Oled1in3Monochrome(
    ic2Device: I2cDevice?, spiDevice: SpiDevice?,
    gpio: Gpio, dcPin: Int?, rstPin: Int,
): Oled1in3Monochrome {
    check((ic2Device == null && spiDevice != null) || (ic2Device != null && spiDevice == null)) { "one of ic2Device or spiDevice must be null" }
    check((ic2Device != null && dcPin == null) || (spiDevice != null && dcPin != null)) { "specify dcPin only for spiDevice" }
    spiDevice?.config = SpiDevice.Config(4_000_000U, 8U, SPI_MODE_3 or SPI_MODE_4WIRE or SPI_MODE_LSB_LAST)
    val dc = if (dcPin == null) null else gpio.output(dcPin, false)
    val rst = tryCatch({
        gpio.output(rstPin, false)
    }) {
        dc?.close()
    }

    fun closeOutputs() {
        tryFinally({
            dc?.close()
        }) {
            rst.close()
        }
    }

    suspend fun init(): Oled1in3Monochrome {
        rst.set(true)
        delay(100.milliseconds)
        rst.set(false)
        delay(100.milliseconds)
        rst.set(true)
        delay(200.milliseconds)

        val oneByte = UByteArray(1)

        fun writeByte(byte: UByte) {
            oneByte[0] = byte
            spiDevice!!.write(oneByte)
        }

        suspend fun command(cmd: UByte) {
            if (ic2Device != null) {
                ic2Device.write(0x00U, cmd)
            } else {
                dc!!.set(false)
                writeByte(cmd)
            }
        }

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
        delay(100.milliseconds)
        command(0xAFU) // turn on oled panel

        val graphics = GraphicsMonochrome(object : Display {
            override val width = WIDTH
            override val height = HEIGHT

            val chunk = UByteArray(32) // bigger chunks don't work

            override suspend fun update(buffer: UByteArray) {
                for (page in 0..<8) {
                    command((0xB0U + page.toUByte()).toUByte()) // set page address
                    command(0x02U) // set low column address
                    command(0x10U) // set high column address
                    var p = WIDTH * page
                    if (ic2Device != null) {
                        for (j in 0..<WIDTH / chunk.size) {
                            val pe = p + chunk.size
                            buffer.copyInto(chunk, 0, p, pe)
                            ic2Device.write(0x40U, chunk)
                            p = pe
                        }
                    } else {
                        dc!!.set(true)
                        for (i in 0..<WIDTH) writeByte(buffer[i + p])
                    }
                }
            }
        })

        return object : Oled1in3Monochrome {
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
