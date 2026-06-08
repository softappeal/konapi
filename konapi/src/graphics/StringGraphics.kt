@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

public class StringGraphics internal constructor(display: Display) : Graphics(display) {
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
                s.append(if (buffer[i++].toInt() == 0) DUMP_PIXEL_OFF else DUMP_PIXEL_ON)
            }
            s.append('\n')
        }
        return s.toString()
    }
}

public fun StringGraphics(width: Int, height: Int): StringGraphics = StringGraphics(object : Display(width, height) {
    override fun update(buffer: UByteArray) {}
})
