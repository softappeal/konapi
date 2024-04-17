@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

public data class Color565(public val red: Int, public var green: Int, public var blue: Int)

public fun Color.toColor565(): Color565 = Color565(
    red = red shr 3,
    green = green shr 2,
    blue = blue shr 3,
)

public data class Color16(public val b1: UByte, public val b2: UByte)

public fun Color565.toColor16(): Color16 = Color16(
    b1 = ((red shl 3) or (green shr 3)).toUByte(),
    b2 = ((green shl 5) or blue).toUByte(),
)

public fun Color.toColor16(): Color16 = toColor565().toColor16()

public class Color16Graphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height * 2)

    private var color16: Color16? = null
    override fun set(color: Color): Graphics {
        super.set(color)
        color16 = color.toColor16()
        return this
    }

    override fun setPixel(x: Int, y: Int) {
        val i = (x + y * width) * 2
        buffer[i] = color16!!.b1
        buffer[i + 1] = color16!!.b2
    }
}
