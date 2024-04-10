@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

import kotlin.test.assertEquals

class TestGraphics(display: Display) : Graphics(display) {
    override val buffer = UByteArray(display.width * display.height)

    override fun setPixel(x: Int, y: Int, color: Color) {
        buffer[x + y * width] = (if (color.notBlack) 1 else 0).toUByte()
    }

    fun dump(): String {
        val s = StringBuilder()
        fun line() {
            s.append(".".repeat(width + 2)).append('\n')
        }
        line()
        var i = 0
        repeat(height) {
            s.append('.')
            repeat(width) {
                s.append(if (buffer[i++].toInt() == 0) ' ' else '#')
            }
            s.append(".\n")
        }
        line()
        return s.toString()
    }
}

fun withGraphics(width: Int, height: Int, action: TestGraphics.() -> Unit) = TestGraphics(object : Display {
    override val width = width
    override val height = height
    override suspend fun update(buffer: UByteArray): Unit = throw NotImplementedError()
}).action()

fun TestGraphics.assert(expected: String) {
    assertEquals(expected.trimIndent() + '\n', dump())
    clear()
}
