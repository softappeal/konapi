package ch.softappeal.konapi.graphics.tools

import ch.softappeal.konapi.assertFailsMessage
import ch.softappeal.konapi.graphics.DummyOverlay
import ch.softappeal.konapi.graphics.FontIcon
import ch.softappeal.konapi.graphics.Icons
import ch.softappeal.konapi.graphics.Overlay
import ch.softappeal.konapi.graphics.dump
import ch.softappeal.konapi.graphics.readOverlayFile
import kotlin.test.Test
import kotlin.test.assertEquals

open class MyIcons(overlay: Overlay) : Icons(overlay) {
    val handSpock = FontIcon(0, 0x1F596)
    val addressCard = FontIcon(1, 0xF2BB)
}

private object MyIconsSmall : MyIcons(readOverlayFile("test-icons/icons.18x18.overlay"))
private object MyIconsBig : MyIcons(readOverlayFile("test-icons/icons.36x34.overlay"))

private const val ICON_FONT = "test-icons/fa-regular-400.ttf"

class CreateOverlayTest {
    @Test
    fun createFonts() {
        createFontsOverlay("fonts")
    }

    @Suppress("unused")
    @Test
    fun createIcons() {
        listOf(16, 32).forEach { size ->
            val overlay = writeIconOverlay(ICON_FONT, size, MyIcons(DummyOverlay), "test-icons/icons")
            println(overlay.dump())
        }
        println(MyIconsSmall.handSpock.index)
        println(MyIconsBig.addressCard.index)
        assertEquals(18, MyIconsSmall.width)
        assertEquals(18, MyIconsSmall.height)
        assertEquals(36, MyIconsBig.width)
        assertEquals(34, MyIconsBig.height)
        assertFailsMessage<IllegalArgumentException>("indexes not in 0..<1") {
            createIconOverlay(ICON_FONT, 16, object : Icons(DummyOverlay) {
                val i1 = FontIcon(1, 0)
            })
        }
        assertFailsMessage<IllegalArgumentException>("duplicated indexes [1, 1]") {
            createIconOverlay(ICON_FONT, 16, object : Icons(DummyOverlay) {
                val i1 = FontIcon(1, 0)
                val i2 = FontIcon(1, 0)
            })
        }
    }
}
