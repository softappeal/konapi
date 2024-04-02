@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public class GraphicsColor(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height * 2)

    override fun setPixel(x: Int, y: Int, color: Color) {
        val k = x * 2 + y * width * 2
        buffer[k] = color.b1
        buffer[k + 1] = color.b2
    }
}
