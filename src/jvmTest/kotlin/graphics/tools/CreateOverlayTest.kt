package ch.softappeal.konapi.graphics.tools

import ch.softappeal.konapi.assertFailsMessage
import ch.softappeal.konapi.graphics.DummyOverlay
import ch.softappeal.konapi.graphics.FontIcon
import ch.softappeal.konapi.graphics.Icons
import ch.softappeal.konapi.graphics.MyFontIcons
import kotlin.test.Test

private const val ICON_FONT = "test-files/fa-regular-400.ttf"

class CreateOverlayTest {
    @Test
    fun createFonts() {
        createFontsOverlay("fonts")
    }

    @Test
    fun createIcons() {
        listOf(16, 32).forEach { size ->
            writeIconOverlay(ICON_FONT, size, MyFontIcons(DummyOverlay), "test-files/icons")
        }
    }

    @Suppress("unused")
    @Test
    fun invalidConfigs() {
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
