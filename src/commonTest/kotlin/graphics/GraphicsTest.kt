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
}

const val TEST_FONT_PATH = "test-files/Test.font"
val TEST_FONT = readOverlaysFile(TEST_FONT_PATH)

object MyIcons : Overlays(TEST_FONT) {
    val a = Icon('a' - FONT_CHARS.first)
    val b = Icon('b' - FONT_CHARS.first)
}

suspend fun Graphics.displayFont(pageDone: suspend () -> Unit) {
    assertFails { font }
    assertFails { draw(0, 0, "hello") }
    set(TEST_FONT)
    assertSame(TEST_FONT, font)
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
    fun graphics() = with(StringGraphics(5, 3)) {
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
        fillRect()
        set(WHITE)
        setPixel(Point(0, 0))
        setPixel(0, height - 1)
        setPixel(width - 1, 0)
        assert("""
            #...#
            .....
            #....
        """)
        set(BLACK).fillRect()
        set(WHITE)
        fillRect(Point(2, 1), Dimensions(3, 2))
        assert("""
            .....
            ..###
            ..###
        """)
    }

    @Test
    fun icons() = with(StringGraphics(MyIcons.width * 2, MyIcons.height)) {
        assertEquals(width / 2, MyIcons.a.width)
        assertEquals(height, MyIcons.a.height)
        set(BLACK).fillRect()
        set(WHITE)
        val ch0 = Point(0, 0)
        val ch1 = Point(MyIcons.width, 0)
        draw(ch0, MyIcons.a)
        draw(ch1, MyIcons.b)
        assert("""
            ............
            ............
            ......#.....
            ......#.....
            .###..#.##..
            ....#.##..#.
            .####.#...#.
            #...#.#...#.
            .####.####..
            ............
        """)
        set(BLACK).fillRect(ch1, MyIcons)
        set(WHITE).draw(ch1, MyIcons.a)
        assert("""
            ............
            ............
            ............
            ............
            .###...###..
            ....#.....#.
            .####..####.
            #...#.#...#.
            .####..####.
            ............
        """)
    }

    @Test
    fun displayFont() = with(StringGraphics(128, 32)) {
        runBlocking {
            displayFont { println(getString()) }
        }
    }
}
