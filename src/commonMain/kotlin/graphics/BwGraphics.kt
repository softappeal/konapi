@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public val Color.notBlack: Boolean get() = (red != 0.toUByte()) || (green != 0.toUByte()) || (blue != 0.toUByte())

public class BwGraphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height / 8)

    private var notBlack: Boolean? = null
    override fun setColor(color: Color): Graphics {
        super.setColor(color)
        notBlack = color.notBlack
        return this
    }

    override fun setPixel(x: Int, y: Int) {
        val i = x + (y / 8) * width
        val b = (1 shl (y % 8)).toUByte()
        buffer[i] = if (notBlack!!) buffer[i] or b else buffer[i] and b.inv()
    }
}
