package ch.softappeal.kopi.graphics.tools

import ch.softappeal.kopi.graphics.BLACK
import ch.softappeal.kopi.graphics.FONT_CHARS
import ch.softappeal.kopi.graphics.Font
import ch.softappeal.kopi.graphics.TEST_FONT
import ch.softappeal.kopi.graphics.WHITE
import ch.softappeal.kopi.graphics.draw
import ch.softappeal.kopi.graphics.fillRect
import ch.softappeal.kopi.graphics.stringGraphics
import ch.softappeal.kopi.graphics.toOverlays
import java.io.File
import java.util.Locale
import kotlin.test.Test

private const val TTF_TEST_FONT = "test-files/Lcd-5x8.ttf"

class CreateFontTest {
    @Test
    fun createTestFont() {
        createFont(TTF_TEST_FONT, 8, TEST_FONT)
    }

    @Test
    fun showFont() {
        val font = Font(createFont(
            File(TTF_TEST_FONT).readBytes(),
            size = 8,
        ).toOverlays())
        val lowerText = "The quick brown fox jumps over the lazy dog".lowercase(Locale.getDefault())
        val upperText = lowerText.uppercase(Locale.getDefault())
        val remainingChars = (FONT_CHARS.toList() - lowerText.toSet() - upperText.toSet()).joinToString(separator = "")
        stringGraphics(font.width * lowerText.length, font.height * 4) {
            set(BLACK).fillRect()
            set(font).set(WHITE)
            draw(0, 0, "width=${font.width} height=${font.height}")
            draw(0, font.height, lowerText)
            draw(0, font.height * 2, upperText)
            draw(0, font.height * 3, remainingChars)
            println(getString())
        }
    }
}
