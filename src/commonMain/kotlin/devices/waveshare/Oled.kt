@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.devices.waveshare

import ch.softappeal.konapi.Closeable
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.I2cDevice
import ch.softappeal.konapi.SPI_MODE_3
import ch.softappeal.konapi.SPI_MODE_4WIRE
import ch.softappeal.konapi.SPI_MODE_MSB_FIRST
import ch.softappeal.konapi.SpiDevice
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.tryCatch
import ch.softappeal.konapi.tryFinally
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

public interface Oled<G : Graphics> : Closeable {
    public val graphics: G
}

public interface OledWriter {
    public val dc: Gpio.Output?
    public fun spiWrite(bytes: UByteArray)
    public fun command(command: UByte)
    public fun spiData(data: UByte)
}

public suspend fun <G : Graphics> Oled(
    i2cDevice: I2cDevice?, spiDevice: SpiDevice?,
    gpio: Gpio, dcPin: Int?, rstPin: Int,
    initSequence: suspend OledWriter.() -> Unit, getGraphics: OledWriter.() -> G,
): Oled<G> {
    check((i2cDevice == null && spiDevice != null) || (i2cDevice != null && spiDevice == null)) { "one of i2cDevice or spiDevice must be null" }
    check((i2cDevice != null && dcPin == null) || (spiDevice != null && dcPin != null)) { "specify dcPin only for spiDevice" }
    spiDevice?.config = SpiDevice.Config(4_000_000, 8, SPI_MODE_3 or SPI_MODE_4WIRE or SPI_MODE_MSB_FIRST)
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

            override fun spiWrite(bytes: UByteArray) {
                spiDevice!!.write(bytes)
            }

            private val oneByte = UByteArray(1)
            private fun spiWrite(byte: UByte) {
                oneByte[0] = byte
                spiDevice!!.write(oneByte)
            }

            override fun command(command: UByte) = if (i2cDevice != null) {
                i2cDevice.write(0x00U, command)
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
        object : Oled<G> {
            override val graphics = graphics
            override fun close() {
                closeOutputs()
            }
        }
    }) {
        closeOutputs()
    }
}
