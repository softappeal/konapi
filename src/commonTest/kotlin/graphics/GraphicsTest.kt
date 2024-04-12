package ch.softappeal.kopi.graphics

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun StringGraphics.assert(expected: String) {
    assertEquals(expected.trimIndent() + '\n', getString())
    set(BLACK).fillRect()
    set(WHITE)
}

const val TEST_FONT = "test-files/Test.font"

suspend fun Graphics.displayFont(pageDone: suspend () -> Unit) {
    assertFails { font }
    assertFails { draw(0, 0, "hello") }
    val testFont = readFontFile(TEST_FONT)
    set(testFont)
    assertSame(testFont, font)
    val chars = FONT_CHARS.iterator()
    while (chars.hasNext()) {
        set(BLACK).fillRect()
        set(BLUE)
        var y = 0
        lines@ for (line in 0..<height / font.height) {
            var x = 0
            for (column in 0..<width / font.width) {
                if (!chars.hasNext()) break@lines
                draw(Point(x, y), chars.nextChar())
                x += font.width
            }
            y += font.height
        }
        pageDone()
    }
}

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
        assertEquals(color565, Color(red = 0b10111111, green = 0b11011111, blue = 0b11101111).toColor565())
        assertEquals(Color16(b1 = 0b10111_110U, b2 = 0b111_11101U), color565.toColor16())
    }

    @Test
    fun graphics() = stringGraphics(5, 3) {
        assertFails { color }
        assertFails { setPixel(0, 0) }
        set(BLACK)
        assertSame(BLACK, color)
        fillRect()
        assert("""
            .....
            .....
            .....
        """)
        setPixel(Point(0, 0))
        setPixel(0, height - 1)
        setPixel(width - 1, 0)
        assert("""
            #...#
            .....
            #....
        """)
        fillRect(Point(2, 1), Dimensions(3, 2))
        assert("""
            .....
            ..###
            ..###
        """)
    }

    @Test
    fun displayFont() = stringGraphics(128, 32) {
        runBlocking {
            displayFont { println(getString()) }
        }
    }
}
