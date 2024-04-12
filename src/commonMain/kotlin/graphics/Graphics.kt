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
    public fun update(buffer: UByteArray)
}

public val DEFAULT_COLOR: Color = BLACK

/**
 * (x       , y         ) pixel is right of x and below of y
 *
 * (0       , 0         ) is top    left
 * (width -1, 0         ) is top    right
 * (0       , height - 1) is bottom left
 * (width -1, height - 1) is bottom right
 */
public abstract class Graphics(private val display: Display) {
    public val width: Int = display.width
    public val height: Int = display.height

    /**
     * see [DEFAULT_COLOR]
     */
    public abstract fun setColor(color: Color): Graphics

    protected abstract val buffer: UByteArray
    public fun update() {
        display.update(buffer)
    }

    public abstract fun setPixel(x: Int, y: Int)
}
