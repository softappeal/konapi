@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

internal data class Color565(val red: Int, var green: Int, var blue: Int)

internal fun Color.toColor565() = Color565(
    red = red shr 3,
    green = green shr 2,
    blue = blue shr 3,
)

internal data class Color16(val b1: UByte, val b2: UByte)

internal fun Color565.toColor16() = Color16(
    b1 = ((red shl 3) or (green shr 3)).toUByte(),
    b2 = ((green shl 5) or blue).toUByte(),
)

internal fun Color.toColor16() = toColor565().toColor16()

public class Color16Graphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(width * height * 2)

    private var color16 = color.toColor16()
    override fun setColorImpl() {
        color16 = color.toColor16()
    }

    override fun setPixelImpl(x: Int, y: Int) {
        val i = (x + y * width) * 2
        buffer[i] = color16.b1
        buffer[i + 1] = color16.b2
    }
}
