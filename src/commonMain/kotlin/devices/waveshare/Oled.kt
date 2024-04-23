@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.devices.waveshare

import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.SPI_MODE_3
import ch.softappeal.konapi.SPI_MODE_4WIRE
import ch.softappeal.konapi.SPI_MODE_MSB_FIRST
import ch.softappeal.konapi.SpiDevice
import ch.softappeal.konapi.SuspendCloseable
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.tryCatch
import ch.softappeal.konapi.tryFinally
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

public interface Oled<G : Graphics> : SuspendCloseable {
    public val graphics: G
}

public interface OledWriter {
    public fun command(command: UByte)
    public fun data(data: UByte)
    public fun data(bytes: UByteArray)
}

public suspend fun <G : Graphics> Oled(
    gpio: Gpio, dcPin: Int, rstPin: Int,
    device: SpiDevice, speedHz: Int,
    initSequence: suspend OledWriter.() -> Unit, getGraphics: OledWriter.() -> G,
): Oled<G> {
    device.config = SpiDevice.Config(speedHz, 8, SPI_MODE_3 or SPI_MODE_4WIRE or SPI_MODE_MSB_FIRST)
    val dc = gpio.output(dcPin, false)
    val rst = tryCatch({
        gpio.output(rstPin, false)
    }) {
        dc.close()
    }

    suspend fun reset() {
        rst.set(true)
        delay(100.milliseconds)
        rst.set(false)
        delay(100.milliseconds)
        rst.set(true)
        delay(100.milliseconds)
    }

    suspend fun closeOutputs() = tryFinally({
        reset()
    }) {
        tryFinally({
            dc.close()
        }) {
            rst.close()
        }
    }

    return tryCatch({
        reset()
        val writer = object : OledWriter {
            private val oneByte = UByteArray(1)
            private fun write(byte: UByte) {
                oneByte[0] = byte
                device.write(oneByte)
            }

            override fun command(command: UByte) {
                dc.set(false)
                write(command)
            }

            override fun data(data: UByte) {
                dc.set(true)
                write(data)
            }

            override fun data(bytes: UByteArray) {
                dc.set(true)
                device.write(bytes)
            }
        }
        writer.initSequence()
        val graphics = writer.getGraphics()
        object : Oled<G> {
            override val graphics = graphics
            override suspend fun close() {
                closeOutputs()
            }
        }
    }) {
        closeOutputs()
    }
}
