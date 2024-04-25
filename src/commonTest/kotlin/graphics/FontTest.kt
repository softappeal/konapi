package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.Closeable
import ch.softappeal.konapi.readFile
import ch.softappeal.konapi.use
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.measureTime

private class FontDesc(fontName: String) {
    val font = readOverlaysFile("fonts/font/$fontName.font")
    val string = fontName + FONT_CHARS.joinToString(separator = "")
}

private val fonts = readFile("test-files/fonts.txt").decodeToString().split('\n').toMutableList().apply { removeLast() }
    .map { FontDesc(it) }

private val colors = listOf(WHITE, RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW)

internal interface DisplayCreator : Closeable {
    fun create()
    val graphics: Graphics
}

internal class Displays(private val displays: List<DisplayCreator>) : Closeable {
    private var fontIndex = 0
    private var colorIndex = 0
    private var displayIndex = 0
    private var pageIndex = 0

    private fun font() = fonts[fontIndex]
    private fun display() = displays[displayIndex]

    init {
        display().create()
        draw()
    }

    private fun numberOfChars() = (display().graphics.width / font().font.width) * (display().graphics.height / font().font.height)
    private fun pages(): Int =
        (font().string.length / numberOfChars()) + (if (font().string.length % numberOfChars() == 0) 0 else 1)

    private fun draw() = with(display().graphics) {
        set(font().font)
        set(BLACK).fillRect()
        set(colors[colorIndex])
        val chars = font().string.substring(pageIndex * numberOfChars()).iterator()
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
        pageIndex = 0
        draw()
    }

    fun prevFont() {
        if (--fontIndex < 0) fontIndex = fonts.size - 1
        pageIndex = 0
        draw()
    }

    fun nextPage() {
        if (++pageIndex >= pages()) pageIndex = 0
        draw()
    }

    fun prevPage() {
        if (--pageIndex < 0) pageIndex = pages() - 1
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

    fun nextDisplay() {
        display().close()
        if (++displayIndex >= displays.size) displayIndex = 0
        display().create()
        pageIndex = 0
        draw()
    }

    fun prevDisplay() {
        display().close()
        if (--displayIndex < 0) displayIndex = displays.size - 1
        display().create()
        pageIndex = 0
        draw()
    }

    override fun close() {
        display().close()
    }
}

private fun displayCreator(width: Int, height: Int) = object : DisplayCreator {
    override fun create() {
        println("create: $width x $height")
    }

    override fun close() {
        println("close: $width x $height")
    }

    override val graphics = StringGraphics(width, height) { println(it) }
}

class FontTest {
    @Test
    @Ignore
    fun test() {
        Displays(listOf(displayCreator(128, 128), displayCreator(128, 64))).use { displays ->
            displays.prevDisplay()
            displays.nextDisplay()
            displays.prevColor()
            displays.nextColor()
            displays.prevFont()
            displays.nextPage()
            displays.prevPage()
            displays.nextFont()
        }
    }
}
