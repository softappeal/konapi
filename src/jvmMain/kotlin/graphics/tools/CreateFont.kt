package ch.softappeal.konapi.graphics.tools

import ch.softappeal.konapi.graphics.AwtGraphics
import ch.softappeal.konapi.graphics.BLACK
import ch.softappeal.konapi.graphics.Dimensions
import ch.softappeal.konapi.graphics.FONT_CHARS
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.Overlays
import ch.softappeal.konapi.graphics.WHITE
import ch.softappeal.konapi.graphics.draw
import ch.softappeal.konapi.graphics.drawImage
import ch.softappeal.konapi.graphics.fillRect
import ch.softappeal.konapi.graphics.toBytes
import java.awt.Color
import java.awt.Font
import java.io.ByteArrayInputStream
import java.util.BitSet
import kotlin.io.path.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

public fun createFont(trueTypeFontPath: String, size: Int): Overlays {
    val font = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(Path(trueTypeFontPath).readBytes()))
        .deriveFont(size.toFloat())

    var width = 0
    var height = 0
    drawImage(1, 1) { // Create a temporary image to get the size of the character
        this.font = font
        width = fontMetrics.stringWidth(FONT_CHARS.first.toString())
        height = fontMetrics.height
    }

    val bitmap = BitSet()
    var bitmapIndex = 0
    for (ch in FONT_CHARS) {
        val image = drawImage(width, height) {
            color = Color.WHITE
            this.font = font
            drawString(ch.toString(), 0, fontMetrics.ascent)
        }
        for (y in 0..<height) {
            for (x in 0..<width) {
                bitmap[bitmapIndex++] = (image.getRGB(x, y) and 0xFF_FF_FF) != 0 // remove alpha
            }
        }
    }
    bitmap[bitmapIndex] = true // needed so that toByteArray below gets all bytes
    return Overlays(FONT_CHARS.count(), Dimensions(width, height), bitmap.toByteArray())
}

public fun <G : Graphics> drawFont(font: Overlays, graphics: (width: Int, height: Int) -> G): G {
    fun Iterable<Char>.join() = this.joinToString(separator = "")
    val upper = ('A'..'Z').join()
    val lower = ('a'..'z').join()
    val digit = ('0'..'9').join() + " <>(){}[]"
    val remaining = (FONT_CHARS.toList() - upper.toSet() - lower.toSet() - digit.toSet()).join()
    return graphics(font.width * upper.length, font.height * 4).apply {
        set(BLACK).fillRect()
        set(font).set(WHITE)
        draw(0, 0, upper)
        draw(0, font.height, lower)
        draw(0, font.height * 2, digit)
        draw(0, font.height * 3, remaining)
    }
}

private const val SIZE_DELIMITER = '-'

public fun createFonts(fontsDir: String) {
    val fontsPath = Path(fontsDir)
    fontsPath.resolve(("ttf")).forEachDirectoryEntry { fontPath ->
        val sizes = mutableListOf<Int>()
        var fontName = fontPath.nameWithoutExtension
        while (true) {
            val size = fontName.substringAfterLast(SIZE_DELIMITER)
            if (fontName == size) break
            sizes.add(size.toInt())
            fontName = fontName.substringBeforeLast(SIZE_DELIMITER)
        }
        sizes.forEach { size ->
            val font = createFont(fontPath.toString(), size)
            fun path(kind: String) = fontsPath.resolve(kind).resolve("$fontName.${font.width}x${font.height}.$kind")
            path("font").writeBytes(font.toBytes())
            drawFont(font) { width, height -> AwtGraphics(width, height) }.writePng(path("png").toString())
        }
    }
}
