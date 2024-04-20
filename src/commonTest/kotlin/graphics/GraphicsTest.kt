package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.assertFailsMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun StringGraphics.assert(expected: String) {
    assertEquals(expected.trimIndent() + '\n', getString())
}

private val TEST_FONT = readOverlaysFile("fonts/font/Hd44780.6x10.font")

private object MyIcons : Overlays(TEST_FONT) {
    val a = Icon('a' - FONT_CHARS.first)
    val b = Icon('b' - FONT_CHARS.first)
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
        assertFailsMessage<IllegalArgumentException>("red=-1 must be in 0..255") { Color(-1, 0, 0) }
        assertFailsMessage<IllegalArgumentException>("green=256 must be in 0..255") { Color(0, 256, 255) }
        assertFailsMessage<IllegalArgumentException>("blue=257 must be in 0..255") { Color(0, 0, 257) }
    }

    @Test
    fun graphics() = with(StringGraphics(5, 3)) {
        assertFails { draw(0, 0, "hello") }
        set(TEST_FONT)
        assertSame(TEST_FONT, font)
        set(BLACK)
        assertSame(BLACK, color)
        fillRect()
        assert("""
            ..........
            ..........
            ..........
        """)
        fillRect()
        set(WHITE)
        setPixel(Point(0, 0))
        setPixel(0, height - 1)
        setPixel(width - 1, 0)
        assert("""
            ##......##
            ..........
            ##........
        """)
        set(BLACK).fillRect()
        set(WHITE)
        fillRect(Rect(Point(2, 1), Dimensions(3, 2)))
        assert("""
            ..........
            ....######
            ....######
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
            ........................
            ........................
            ............##..........
            ............##..........
            ..######....##..####....
            ........##..####....##..
            ..########..##......##..
            ##......##..##......##..
            ..########..########....
            ........................
        """)
        set(BLACK).fillRect(ch1, MyIcons)
        set(WHITE).draw(ch1, MyIcons.a)
        assert("""
            ........................
            ........................
            ........................
            ........................
            ..######......######....
            ........##..........##..
            ..########....########..
            ##......##..##......##..
            ..########....########..
            ........................
        """)
    }

    @Test
    fun dumpFont() {
        val overlays = TEST_FONT
        val dump = overlays.dump()
        // print(dump)
        assertEquals(dump, Overlays(overlays.size, overlays, dump).dump())
    }

    @Test
    fun dump() {
        val overlays = Overlays(2, Dimensions(3, 2), """
            0
            ##....
            ......
            1
            ......
            ....##
        """)
        with(StringGraphics(overlays.width * 2, overlays.height)) {
            set(BLACK).fillRect()
            set(WHITE)
            overlays.draw(this, 0, 0, 0)
            overlays.draw(this, overlays.width, 0, 1)
            assert("""
                ##..........
                ..........##
            """)
        }
        assertFailsMessage<IllegalArgumentException>("index 0 expected (actual is 1)") {
            Overlays(2, Dimensions(3, 2), """
                1
            """)
        }
        assertFailsMessage<IllegalArgumentException>("wrong line width at index 0 (2 instead of 6)") {
            Overlays(2, Dimensions(3, 2), """
                0
                ..
            """)
        }
        assertFailsMessage<IllegalArgumentException>("unexpected pixel '12' at index 0") {
            Overlays(2, Dimensions(3, 2), """
                0
                123456
            """)
        }
        assertFailsMessage<IllegalArgumentException>("unexpected lines at end") {
            Overlays(2, Dimensions(3, 2), """
                0
                ......
                ......
                1
                ......
                ......

            """)
        }
    }
}
