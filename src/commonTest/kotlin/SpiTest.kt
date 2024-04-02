@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

abstract class SpiTest {
    @Test
    fun test() {
        spiDevice().use { spi ->
            println("blockSize: ${spi.blockSize}")
            println(spi.config)
            val config = SpiDevice.Config(speedHz = 10_000_000U, bitsPerWord = 8U, mode = SPI_MODE_3)
            println(config)
            spi.config = config
            assertEquals(config, spi.config)
            val config2 = SpiDevice.Config(mode = SPI_MODE_0)
            spi.config = config2
            assertEquals(config.copy(mode = config2.mode), spi.config)
            println(spi.config)
            val bytes = UByteArray(2 * spi.blockSize + 97) { (it % 113).toUByte() }
            val copy = bytes.copyOf()
            val transferTime = measureTime { spi.transfer(bytes) }
            println("transferTime = $transferTime")
            for (i in bytes.indices) assertEquals(copy[i], bytes[i])
        }
    }
}
