@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

public const val STRING_PIXEL_WIDTH: Int = 2 // needed for near quadratic output
public const val STRING_PIXEL_ON: String = "##"
public const val STRING_PIXEL_OFF: String = ".."

public class StringGraphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(width * height)

    private var notBlack = color.notBlack
    override fun setColorImpl() {
        notBlack = color.notBlack
    }

    override fun setPixelImpl(x: Int, y: Int) {
        buffer[x + y * width] = (if (notBlack) 1 else 0).toUByte()
    }

    public fun getString(): String {
        val s = StringBuilder()
        var i = 0
        repeat(height) {
            repeat(width) {
                s.append(if (buffer[i++].toInt() == 0) STRING_PIXEL_OFF else STRING_PIXEL_ON)
            }
            s.append('\n')
        }
        return s.toString()
    }
}

public fun StringGraphics(width: Int, height: Int, update: (string: String) -> Unit = {}): StringGraphics {
    lateinit var graphics: StringGraphics
    graphics = StringGraphics(object : Display(width, height) {
        override fun update(buffer: UByteArray) {
            update(graphics.getString())
        }
    })
    return graphics
}
