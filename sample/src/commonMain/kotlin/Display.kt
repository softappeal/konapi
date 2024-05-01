package sample

import ch.softappeal.konapi.graphics.BLACK
import ch.softappeal.konapi.graphics.BLUE
import ch.softappeal.konapi.graphics.CYAN
import ch.softappeal.konapi.graphics.FONT_CHARS
import ch.softappeal.konapi.graphics.GREEN
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.MAGENTA
import ch.softappeal.konapi.graphics.RED
import ch.softappeal.konapi.graphics.WHITE
import ch.softappeal.konapi.graphics.YELLOW
import ch.softappeal.konapi.graphics.draw
import ch.softappeal.konapi.graphics.fillRect
import ch.softappeal.konapi.graphics.readOverlayFile
import ch.softappeal.konapi.readFile
import kotlin.time.measureTime

private class Font(private val name: String) {
    val font = readOverlayFile("font/$name")
    fun chars() = (name.substringBeforeLast(".font") + FONT_CHARS.joinToString(separator = "")).iterator()
}

private val fonts = readFile("fonts.txt").decodeToString().split('\n').toMutableList().apply { removeLast() }
    .map { Font(it) }

private val colors = listOf(WHITE, RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW)

class Display(private val graphics: Graphics) {
    private var fontIndex = 0
    private var colorIndex = 0

    init {
        draw()
    }

    private fun draw() = with(graphics) {
        val f = fonts[fontIndex]
        set(f.font)
        set(BLACK).fillRect()
        set(colors[colorIndex])
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
        println("update: ${measureTime { update() }}")
    }

    fun nextFont() {
        if (++fontIndex >= fonts.size) fontIndex = 0
        draw()
    }

    fun prevFont() {
        if (--fontIndex < 0) fontIndex = fonts.size - 1
        draw()
    }

    fun nextColor() {
        if (++colorIndex >= colors.size) colorIndex = 0
        draw()
    }

    fun prevColor() {
        if (--colorIndex < 0) colorIndex = colors.size - 1
        draw()
    }
}
