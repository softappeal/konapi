@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public class Color(red: UByte, green: UByte, blue: UByte) {
    internal val b1: UByte
    internal val b2: UByte

    init {
        val rb = (red.toInt() and 0xF8) shl 8
        val rg = (green.toInt() and 0xFC) shl 3
        val bb = blue.toInt() shr 3
        val us = (rb or rg or bb).toUShort()
        b1 = (us.toInt() shr 8).toUByte()
        b2 = us.toUByte()
    }
}

public val BLACK: Color = Color(0x00U, 0x00U, 0x00U)
public val WHITE: Color = Color(0xFFU, 0xFFU, 0xFFU)
public val RED: Color = Color(0xFFU, 0x00U, 0x00U)
public val GREEN: Color = Color(0x00U, 0xFFU, 0x00U)
public val BLUE: Color = Color(0x00U, 0x00U, 0xFFU)
public val CYAN: Color = Color(0x00U, 0xFFU, 0xFFU)
public val MAGENTA: Color = Color(0xFFU, 0x00U, 0xFFU)
public val YELLOW: Color = Color(0xFFU, 0xFFU, 0x00U)

public interface Display {
    public val width: Int
    public val height: Int
    public fun update(buffer: UByteArray)
}

public abstract class Graphics(private val display: Display) {
    public val width: Int = display.width
    public val height: Int = display.height

    protected abstract val buffer: UByteArray
    public fun update() {
        display.update(buffer)
    }

    public abstract fun setPixel(x: Int, y: Int, color: Color)
}

public fun Graphics.fillRect(x: Int, y: Int, w: Int, h: Int, color: Color) {
    for (xCoord in x..<x + w) {
        for (yCoord in y..<y + h) {
            setPixel(xCoord, yCoord, color)
        }
    }
}

public fun Graphics.clear() {
    fillRect(0, 0, width, height, BLACK)
}
