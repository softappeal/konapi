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
    require(size > 0) { "size=$size must be > 0" }
    val font = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(Path(trueTypeFontPath).readBytes()))
        .deriveFont(size.toFloat())
    val bitmap = BitSet()
    var bitmapIndex = 0
    val dimensions = drawImage(128, 128) /* should be big enough for all chars */ { image ->
        this.font = font
        val width = fontMetrics.stringWidth(FONT_CHARS.first.toString())
        val height = fontMetrics.height
        for (ch in FONT_CHARS) {
            val s = ch.toString()
            check(fontMetrics.stringWidth(s) == width) { "char ${ch.code} has wrong width" }
            color = Color.BLACK
            fillRect(0, 0, width, height)
            color = Color.WHITE
            drawString(s, 0, fontMetrics.ascent)
            for (y in 0..<height) {
                for (x in 0..<width) {
                    bitmap[bitmapIndex++] = (image.getRGB(x, y) and 0xFF_FF_FF) != 0 // remove alpha
                }
            }
        }
        Dimensions(width, height)
    }
    bitmap[bitmapIndex] = true // needed so that toByteArray below gets all bytes
    return Overlays(FONT_CHARS.count(), dimensions, bitmap.toByteArray())
}

public fun <G : Graphics> drawFont(font: Overlays, graphics: (dimensions: Dimensions) -> G): G {
    fun Iterable<Char>.join() = this.joinToString(separator = "")
    val upper = ('A'..'Z').join()
    val lower = ('a'..'z').join()
    val digit = ('0'..'9').join() + " <>(){}[]"
    val remaining = (FONT_CHARS.toList() - upper.toSet() - lower.toSet() - digit.toSet()).join()
    return graphics(Dimensions(font.width * upper.length, font.height * 4)).apply {
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
            drawFont(font) { dimensions -> AwtGraphics(4, dimensions) }.writePng(path("png").toString())
        }
    }
}
