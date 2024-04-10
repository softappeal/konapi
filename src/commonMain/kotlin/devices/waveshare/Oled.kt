@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.devices.waveshare

import ch.softappeal.kopi.Closeable
import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.SPI_MODE_3
import ch.softappeal.kopi.SPI_MODE_4WIRE
import ch.softappeal.kopi.SPI_MODE_LSB_LAST
import ch.softappeal.kopi.SpiDevice
import ch.softappeal.kopi.graphics.Graphics
import ch.softappeal.kopi.tryCatch
import ch.softappeal.kopi.tryFinally
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

public interface Oled : Closeable {
    public val graphics: Graphics
}

public interface OledWriter {
    public val dc: Gpio.Output?
    public fun spiWrite(byte: UByte)
    public suspend fun command(command: UByte)
    public fun spiData(data: UByte)
}

public suspend fun Oled(
    ic2Device: I2cDevice?, spiDevice: SpiDevice?,
    gpio: Gpio, dcPin: Int?, rstPin: Int,
    initSequence: suspend OledWriter.() -> Unit, getGraphics: OledWriter.() -> Graphics,
): Oled {
    check((ic2Device == null && spiDevice != null) || (ic2Device != null && spiDevice == null)) { "one of ic2Device or spiDevice must be null" }
    check((ic2Device != null && dcPin == null) || (spiDevice != null && dcPin != null)) { "specify dcPin only for spiDevice" }
    spiDevice?.config = SpiDevice.Config(4_000_000U, 8U, SPI_MODE_3 or SPI_MODE_4WIRE or SPI_MODE_LSB_LAST)
    val dc = if (dcPin == null) null else gpio.output(dcPin, false)
    val rst = tryCatch({
        gpio.output(rstPin, false)
    }) {
        dc?.close()
    }

    fun closeOutputs() = tryFinally({
        dc?.close()
    }) {
        rst.close()
    }

    return tryCatch({
        rst.set(true)
        delay(100.milliseconds)
        rst.set(false)
        delay(100.milliseconds)
        rst.set(true)
        delay(100.milliseconds)
        val writer = object : OledWriter {
            override val dc = dc

            private val oneByte = UByteArray(1)
            override fun spiWrite(byte: UByte) {
                oneByte[0] = byte
                spiDevice!!.write(oneByte)
            }

            override suspend fun command(command: UByte) = if (ic2Device != null) {
                ic2Device.write(0x00U, command)
            } else {
                dc!!.set(false)
                spiWrite(command)
            }

            override fun spiData(data: UByte) {
                dc!!.set(true)
                spiWrite(data)
            }
        }
        writer.initSequence()
        val graphics = writer.getGraphics()
        object : Oled {
            override val graphics = graphics
            override fun close() {
                closeOutputs()
            }
        }
    }) {
        closeOutputs()
    }
}
