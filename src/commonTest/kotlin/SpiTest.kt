@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime

abstract class SpiTest {
    @Test
    fun test() {
        spiDevice().use { spi ->
            val bytes = UByteArray(4000) { (it % 100).toUByte() }
            val copy = bytes.copyOf()
            val transferTime = measureTime { spi.transfer(bytes) }
            println("transferTime = $transferTime")
            for (i in bytes.indices) assertEquals(copy[i], bytes[i])
        }
    }
}
