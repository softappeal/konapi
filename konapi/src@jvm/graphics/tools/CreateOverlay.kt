package ch.softappeal.konapi.graphics.tools

import ch.softappeal.konapi.graphics.AwtGraphics
import ch.softappeal.konapi.graphics.BLACK
import ch.softappeal.konapi.graphics.Dimension
import ch.softappeal.konapi.graphics.FONT_CHARS
import ch.softappeal.konapi.graphics.FontIcon
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.Icons
import ch.softappeal.konapi.graphics.Overlay
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
import kotlin.reflect.full.declaredMemberProperties

private const val IMAGE_WIDTH = 128
private const val IMAGE_HEIGHT = 128

public fun createOverlay(trueTypeFontPath: String, size: Int, codePoints: List<Int>): Overlay {
    require(size > 0) { "size=$size must be > 0" }
    val font = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(Path(trueTypeFontPath).readBytes()))
        .deriveFont(size.toFloat())
    val bitmap = BitSet()
    var bitmapIndex = 0
    val dimension = drawImage(IMAGE_WIDTH, IMAGE_HEIGHT) { image ->
        this.font = font
        val width = fontMetrics.stringWidth(Character.toString(codePoints.first()))
        val height = fontMetrics.height
        check(width <= IMAGE_WIDTH)
        check(height <= IMAGE_HEIGHT)
        for (codePoint in codePoints) {
            val s = Character.toString(codePoint)
            check(fontMetrics.stringWidth(s) == width) { "codePoint $codePoint has wrong width" }
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
        Dimension(width, height)
    }
    bitmap[bitmapIndex] = true // needed so that toByteArray below gets all bytes
    return Overlay(codePoints.size, dimension, bitmap.toByteArray())
}

public fun createFontOverlay(trueTypeFontPath: String, size: Int): Overlay =
    createOverlay(trueTypeFontPath, size, FONT_CHARS.map { it.code })

public fun <G : Graphics> drawFont(font: Overlay, graphics: (dimension: Dimension) -> G): G {
    fun Iterable<Char>.join() = this.joinToString(separator = "")
    val upper = ('A'..'Z').join()
    val lower = ('a'..'z').join()
    val digit = ('0'..'9').join() + " <>(){}[]"
    val remaining = (FONT_CHARS.toList() - upper.toSet() - lower.toSet() - digit.toSet()).join()
    return graphics(Dimension(font.width * upper.length, font.height * 4)).apply {
        set(BLACK).fillRect()
        set(font).set(WHITE)
        draw(0, 0, upper)
        draw(0, font.height, lower)
        draw(0, font.height * 2, digit)
        draw(0, font.height * 3, remaining)
    }
}

public fun Overlay.writeFile(filePath: String) {
    Path(filePath).writeBytes(toBytes())
}

public const val FONT_SIZE_DELIMITER: Char = '-'

public fun createFontsOverlay(fontsDir: String) {
    val fontsPath = Path(fontsDir)
    fontsPath.resolve(("ttf")).forEachDirectoryEntry { fontPath ->
        val sizes = mutableListOf<Int>()
        var fontName = fontPath.nameWithoutExtension
        while (true) {
            val size = fontName.substringAfterLast(FONT_SIZE_DELIMITER)
            if (fontName == size) break
            sizes.add(size.toInt())
            fontName = fontName.substringBeforeLast(FONT_SIZE_DELIMITER)
        }
        sizes.forEach { size ->
            val font = createFontOverlay(fontPath.toString(), size)
            fun path(kind: String) = fontsPath.resolve(kind).resolve("$fontName.${font.width}x${font.height}.$kind").toString()
            font.writeFile(path("font"))
            drawFont(font) { dimension -> AwtGraphics(4, dimension) }.writePng(path("png"))
        }
    }
}

public fun createIconOverlay(trueTypeFontPath: String, size: Int, icons: Icons): Overlay {
    val fontIcons = icons::class.declaredMemberProperties
        .map { it.getter.call(icons) as FontIcon }
        .sortedBy { it.index }
    require(fontIcons.map { it.index }.toSet().size == fontIcons.size) { "duplicated indexes ${fontIcons.map { it.index }}" }
    @Suppress("SimplifiableCallChain")
    require(fontIcons.map { it.index }.max() + 1 == fontIcons.size) { "indexes not in 0..<${fontIcons.size}" }
    return createOverlay(trueTypeFontPath, size, fontIcons.map { it.codePoint })
}

public fun writeIconOverlay(trueTypeFontPath: String, size: Int, icons: Icons, overlayPathPrefix: String): Overlay {
    val overlay = createIconOverlay(trueTypeFontPath, size, icons)
    overlay.writeFile("$overlayPathPrefix.${overlay.width}x${overlay.height}.overlay")
    return overlay
}
