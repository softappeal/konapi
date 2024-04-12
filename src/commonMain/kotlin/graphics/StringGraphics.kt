@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public class StringGraphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height)

    private var notBlack: Boolean? = null
    override fun set(color: Color): Graphics {
        super.set(color)
        notBlack = color.notBlack
        return this
    }

    override fun setPixel(x: Int, y: Int) {
        buffer[x + y * width] = (if (notBlack!!) 1 else 0).toUByte()
    }

    public fun getString(): String {
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

public fun stringGraphics(width: Int, height: Int, action: StringGraphics.() -> Unit) {
    StringGraphics(object : Display(width, height) {
        override fun update(buffer: UByteArray): Unit = throw NotImplementedError()
    }).action()
}
