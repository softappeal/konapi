@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

internal data class Color565(val red: Int, var green: Int, var blue: Int)

internal fun Color.toColor565() = Color565(
    red = red.toInt() shr 3,
    green = green.toInt() shr 2,
    blue = blue.toInt() shr 3,
)

internal data class Color16(val b1: UByte, val b2: UByte)

internal fun Color565.toColor16() = Color16(
    b1 = ((red shl 3) or (green shr 3)).toUByte(),
    b2 = ((green shl 5) or blue).toUByte(),
)

public class Color16Graphics(display: Display) : Graphics(display) {
    override val buffer: UByteArray = UByteArray(display.width * display.height * 2)

    override fun setPixel(x: Int, y: Int, color: Color) {
        val i = (x + y * width) * 2
        val color16 = color.toColor565().toColor16()
        buffer[i] = color16.b1
        buffer[i + 1] = color16.b2
    }
}
