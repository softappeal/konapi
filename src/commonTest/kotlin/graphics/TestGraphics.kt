@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

import kotlin.test.assertEquals

class TestGraphics(display: Display) : Graphics(display) {
    override val buffer = UByteArray(display.width * display.height)

    private var notBlack: Boolean? = null
    override fun set(color: Color): Graphics {
        super.set(color)
        notBlack = color.notBlack
        return this
    }

    override fun setPixel(x: Int, y: Int) {
        buffer[x + y * width] = (if (notBlack!!) 1 else 0).toUByte()
    }

    fun dump(): String {
        val s = StringBuilder()
        var i = 0
        repeat(height) {
            repeat(width) {
                s.append(if (buffer[i++].toInt() == 0) '.' else '#')
            }
            s.append('\n')
        }
        return s.toString()
    }
}

fun withGraphics(width: Int, height: Int, action: TestGraphics.() -> Unit) = TestGraphics(object : Display(width, height) {
    override fun update(buffer: UByteArray): Unit = throw NotImplementedError()
}).action()

fun TestGraphics.assert(expected: String) {
    assertEquals(expected.trimIndent() + '\n', dump())
    set(BLACK).fillRect()
    set(WHITE)
}
