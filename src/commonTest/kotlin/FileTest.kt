package ch.softappeal.konapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class FileTest {
    @Test
    fun test() {
        println(assertFails { readFile("test-files/no-such-file") })
        val bytes = readFile("test-files/read.test.bin")
        assertEquals(3, bytes.size)
        assertEquals(-1, bytes[0])
        assertEquals(1, bytes[1])
        assertEquals(123, bytes[2])
    }
}
