package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.assertFailsMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.measureTime

fun Graphics.testDisplay() {
    set(BLACK).fillRect()
    var x = 0
    val colors = listOf(RED, GREEN, BLUE, BLACK, WHITE, CYAN, MAGENTA, YELLOW)
    val stripes = 8
    val w = width / colors.size / stripes
    colors.forEach { c ->
        repeat(stripes) { s ->
            fun map(color: Int) = color / (s + 1)
            color = Color(map(c.red), map(c.green), map(c.blue))
            fillRect(x, 0, w, height - (s * 5))
            x += w
        }
    }
    println("update: ${measureTime { update() }}")
}

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

open class MyFontIcons(overlay: Overlay) : Icons(overlay) {
    val handSpock = FontIcon(0, 0x1F596)
    val addressCard = FontIcon(1, 0xF2BB)
}

private object MyIconsSmall : MyFontIcons(readOverlayFile("test-files/icons.18x18.overlay"))
private object MyIconsBig : MyFontIcons(readOverlayFile("test-files/icons.36x34.overlay"))

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
    fun fontIcons() {
        println(MyIconsSmall.overlay.dump())
        println(MyIconsBig.overlay.dump())
        assertEquals(0, MyIconsSmall.handSpock.index)
        assertEquals(1, MyIconsBig.addressCard.index)
        assertEquals(18, MyIconsSmall.width)
        assertEquals(18, MyIconsSmall.height)
        assertEquals(36, MyIconsBig.width)
        assertEquals(34, MyIconsBig.height)
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
}
