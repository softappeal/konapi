package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.assertFailsMessage
import ch.softappeal.konapi.readFile
import ch.softappeal.konapi.sleepMs
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun StringGraphics.assert(expected: String) {
    assertEquals(expected.trimIndent() + '\n', getString())
}

private object MyIcons : Icons(Overlay(2, Dimension(3, 2), """
    0
    ##....
    ......
    1
    ......
    ....##
""")) {
    val topLeft = Icon(0)
    val bottomRight = Icon(1)
}

class GraphicsTest {
    @Test
    fun graphics() = with(StringGraphics(5, 3)) {
        set(RED)
        assertSame(this, set(MAGENTA))
        assertSame(MAGENTA, color)
        assertEquals(123, retainColor {
            color = CYAN
            123
        })
        assertEquals(MAGENTA, color)
        set(BLACK).fillRect()
        assert("""
            ..........
            ..........
            ..........
        """)
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
        fillRect(Rect(Point(2, 1), Dimension(3, 2)))
        assert("""
            ..........
            ....######
            ....######
        """)
    }

    @Test
    fun icons() {
        assertEquals(MyIcons.width, MyIcons.topLeft.width)
        assertEquals(MyIcons.height, MyIcons.bottomRight.height)
        with(StringGraphics(MyIcons.width * 2, MyIcons.height)) {
            set(BLACK).fillRect()
            set(WHITE)
            draw(0, 0, FontIcon(MyIcons.overlay, MyIcons.topLeft.index, 0))
            draw(Point(MyIcons.topLeft.width, 0), MyIcons.bottomRight)
            assert("""
                ##..........
                ..........##
            """)
        }
    }

    @Test
    fun drawFont() {
        val testFont = readOverlayFile("test-files/test.font")
        with(StringGraphics(testFont.width * 2, testFont.height)) {
            assertFails { draw(Point(0, 0), 'h') }
            assertSame(this, set(testFont))
            assertSame(testFont, font)
            set(BLACK).fillRect()
            set(WHITE).draw(Point(0, 0), "ab")
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
        }
    }

    @Test
    fun parseOverlay() {
        val overlay = MyIcons.overlay
        val dump = overlay.dump()
        print(dump)
        assertEquals(dump, Overlay(overlay.size, overlay, dump).dump())
        assertFailsMessage<IllegalArgumentException>("index 0 expected (actual is 1)") {
            Overlay(2, Dimension(3, 2), """
                1
            """)
        }
        assertFailsMessage<IllegalArgumentException>("wrong line width at index 0 (2 instead of 6)") {
            Overlay(2, Dimension(3, 2), """
                0
                ..
            """)
        }
        assertFailsMessage<IllegalArgumentException>("unexpected pixel '12' at index 0") {
            Overlay(2, Dimension(3, 2), """
                0
                123456
            """)
        }
        assertFailsMessage<IllegalArgumentException>("unexpected lines at end") {
            Overlay(2, Dimension(3, 2), """
                0
                ......
                ......
                1
                ......
                ......

            """)
        }
    }

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
    @Ignore
    fun testDisplay() {
        AwtGraphics(4, Dimension(128, 64)).apply {
            testDisplay()
        }.showWindow(100, 200)
        sleepMs(5_000)
    }

    @Test
    @Ignore
    fun showImage() {
        AwtGraphics(4, Dimension(150, 150)).apply {
            set(WHITE).fillRect()
            draw(10, 10, RawImage(128, 128, readFile("test-files/me.128x128.rgb.raw")))
        }.showWindow(100, 200)
        sleepMs(5_000)
    }
}
