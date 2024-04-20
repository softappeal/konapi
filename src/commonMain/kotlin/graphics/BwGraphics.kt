@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

internal val Color.notBlack get() = (red != 0) || (green != 0) || (blue != 0)

public class BwGraphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(width * height / 8)

    private var notBlack = color.notBlack
    override fun setColorImpl() {
        notBlack = color.notBlack
    }

    override fun setPixelImpl(x: Int, y: Int) {
        val i = x + (y / 8) * width
        val b = (1 shl (y % 8)).toUByte()
        buffer[i] = if (notBlack) buffer[i] or b else buffer[i] and b.inv()
    }
}
