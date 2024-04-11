@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FileTest {
    @Test
    fun test() {
        println(assertFails { readFile("test-files/no-such-file") })
        val bytes = readFile("test-files/read.test.bin")
        assertEquals(3, bytes.size)
        assertEquals(0xFFU, bytes[0])
        assertEquals(0x01U, bytes[1])
        assertEquals(123U, bytes[2])
    }
}
