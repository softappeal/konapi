package sample

import ch.softappeal.konapi.graphics.FONT_CHARS
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.draw
import ch.softappeal.konapi.graphics.readOverlayFile
import ch.softappeal.konapi.readFile

private class Font(private val name: String) {
    val font = readOverlayFile("$filesDir/$name")
    fun chars() = (name.substringBeforeLast(".font") + FONT_CHARS.joinToString(separator = "")).iterator()
}

private val fonts = readFile("$filesDir/fonts.txt").decodeToString().split('\n').toMutableList().apply { removeLast() }
    .map { Font(it) }

class FontView(graphics: Graphics) : View(graphics) {
    private var fontIndex = 0

    override fun nextPage() {
        if (++fontIndex >= fonts.size) fontIndex = 0
    }

    override fun prevPage() {
        if (--fontIndex < 0) fontIndex = fonts.size - 1
    }

    override fun Graphics.drawImpl() {
        val f = fonts[fontIndex]
        set(f.font)
        val chars = f.chars()
        var y = 0
        lines@ for (line in 0..<height / font.height) {
            var x = 0
            for (column in 0..<width / font.width) {
                if (!chars.hasNext()) break@lines
                draw(x, y, chars.nextChar())
                x += font.width
            }
            y += font.height
        }
    }
}
