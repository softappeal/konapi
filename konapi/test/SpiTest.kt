@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

abstract class SpiTest {
    @Test
    fun test() {
        colorDisplay().use { device ->
            println("blockSize: ${device.blockSize}")
            println(device.config)
            val config = SpiDevice.Config(10_000_000, 8, SPI_MODE_3 or SPI_MODE_4WIRE or SPI_MODE_MSB_FIRST)
            println(config)
            device.config = config
            assertEquals(config, device.config)
            val bytes = UByteArray(2 * device.blockSize + 97) { (it % 113).toUByte() }
            val copy = bytes.copyOf()
            val transferTime = measureTime { device.transfer(bytes) }
            println("transferTime = $transferTime")
            for (i in bytes.indices) assertEquals(copy[i], bytes[i])
        }
    }
}
