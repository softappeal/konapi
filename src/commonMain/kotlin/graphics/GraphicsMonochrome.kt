@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public class GraphicsMonochrome(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height / 8)

    override fun setPixel(x: Int, y: Int, color: Color) {
        val k = x + (y / 8) * width
        val white = (color.b1.toInt() != 0) or (color.b2.toInt() != 0)
        val b = (1 shl (y % 8)).toUByte()
        buffer[k] = if (white) buffer[k] or b else buffer[k] and b.inv()
    }
}
