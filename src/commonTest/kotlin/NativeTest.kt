package ch.softappeal.konapi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class NativeTest {
    @Test
    fun sleepMs() {
        assertFails { sleepMs(-1) }
        assertTrue(measureTime { sleepMs(0) } in 0.milliseconds..1.milliseconds)
        assertTrue(measureTime { sleepMs(1) } in 0.milliseconds..10.milliseconds)
        assertTrue(measureTime { sleepMs(12) } in 11.milliseconds..21.milliseconds)
        assertTrue(measureTime { sleepMs(1100) } in 1100.milliseconds..1110.milliseconds)
    }

    @Test
    fun file() {
        println(assertFails { readFile("test-files/no-such-file") })
        val bytes = readFile("test-files/read.test.bin")
        assertEquals(3, bytes.size)
        assertEquals(-1, bytes[0])
        assertEquals(1, bytes[1])
        assertEquals(123, bytes[2])
    }
}
