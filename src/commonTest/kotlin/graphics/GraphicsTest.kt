package ch.softappeal.kopi.graphics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphicsTest {
    @Test
    fun bw() {
        assertTrue(RED.notBlack)
        assertTrue(GREEN.notBlack)
        assertTrue(BLUE.notBlack)
        assertTrue(WHITE.notBlack)
        assertTrue(MAGENTA.notBlack)
        assertTrue(CYAN.notBlack)
        assertTrue(YELLOW.notBlack)
        println(BLACK)
        assertFalse(BLACK.notBlack)
    }

    @Test
    fun color() {
        val color565 = Color565(red = 0b10111, green = 0b110_111, blue = 0b11101)
        assertEquals(color565, Color(red = 0b10111111U, green = 0b11011111U, blue = 0b11101111U).toColor565())
        assertEquals(Color16(b1 = 0b10111_110U, b2 = 0b111_11101U), color565.toColor16())
    }

    @Test
    fun graphics() = withGraphics(5, 3) {
        setPixel(0, 0, WHITE)
        setPixel(0, height - 1, WHITE)
        setPixel(width - 1, 0, WHITE)
        assert("""
            .......
            .#   #.
            .     .
            .#    .
            .......
        """)
        fillRect(2, 1, 3, 2, WHITE)
        assert("""
            .......
            .     .
            .  ###.
            .  ###.
            .......
        """)
    }
}
