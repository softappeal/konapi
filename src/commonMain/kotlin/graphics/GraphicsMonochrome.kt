@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public class GraphicsMonochrome(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height / 8)

    override fun setPixel(x: Int, y: Int, color: Color) {
        val i = x + (y / 8) * width
        val b = (1 shl (y % 8)).toUByte()
        buffer[i] = if (color.notBlack) buffer[i] or b else buffer[i] and b.inv()
    }
}
