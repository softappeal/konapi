@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public data class Color(public val red: UByte, public val green: UByte, public val blue: UByte)

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
    public suspend fun update(buffer: UByteArray)
}

public abstract class Graphics(private val display: Display) {
    public val width: Int = display.width
    public val height: Int = display.height

    protected abstract val buffer: UByteArray
    public suspend fun update() {
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
