package ch.softappeal.konapi.graphics.tools

import ch.softappeal.konapi.graphics.FONT_CHARS
import ch.softappeal.konapi.graphics.Overlays
import ch.softappeal.konapi.graphics.toBytes
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.BitSet

private const val IMAGE_TYPE = BufferedImage.TYPE_BYTE_BINARY

public fun createFont(trueTypeFont: ByteArray, size: Int): ByteArray {
    val font = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(trueTypeFont))
        .deriveFont(size.toFloat())

    val width: Int
    val height: Int
    run { // Create a temporary image to get the size of the character
        val image = BufferedImage(1, 1, IMAGE_TYPE)
        val graphics = image.createGraphics()
        try {
            graphics.font = font
            width = graphics.fontMetrics.stringWidth(FONT_CHARS.first.toString())
            height = graphics.fontMetrics.height
        } finally {
            graphics.dispose()
        }
    }

    val bitmap = BitSet()
    var bitmapIndex = 0

    fun addChar(ch: Char) {
        val image = BufferedImage(width, height, IMAGE_TYPE)
        run {
            val graphics = image.createGraphics()
            try {
                graphics.color = Color.WHITE
                graphics.font = font
                graphics.drawString(ch.toString(), 0, graphics.fontMetrics.ascent)
            } finally {
                graphics.dispose()
            }
        }
        for (y in 0..<height) {
            for (x in 0..<width) {
                bitmap[bitmapIndex++] = (image.getRGB(x, y) and 0xFF_FF_FF) != 0 // remove alpha
            }
        }
    }

    for (ch in FONT_CHARS) addChar(ch)
    bitmap[bitmapIndex] = true // needed so that toByteArray below gets all bytes
    return Overlays(FONT_CHARS.count(), width, height, bitmap.toByteArray()).toBytes()
}

public fun createFont(trueTypeFontPath: String, size: Int, createdFontPath: String) {
    File(createdFontPath).writeBytes(
        createFont(File(trueTypeFontPath).readBytes(), size)
    )
}
