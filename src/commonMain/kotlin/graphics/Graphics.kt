@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

public data class Color(public val red: Int, public val green: Int, public val blue: Int)

public val BLACK: Color = Color(0x00, 0x00, 0x00)
public val WHITE: Color = Color(0xFF, 0xFF, 0xFF)
public val RED: Color = Color(0xFF, 0x00, 0x00)
public val GREEN: Color = Color(0x00, 0xFF, 0x00)
public val BLUE: Color = Color(0x00, 0x00, 0xFF)
public val CYAN: Color = Color(0x00, 0xFF, 0xFF)
public val MAGENTA: Color = Color(0xFF, 0x00, 0xFF)
public val YELLOW: Color = Color(0xFF, 0xFF, 0x00)

public interface Display {
    public val width: Int
    public val height: Int
    public fun update(buffer: UByteArray)
}

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

    private var _color: Color? = null
    public val color: Color get() = _color!!
    public open fun setColor(color: Color): Graphics {
        _color = color
        return this
    }

    protected abstract val buffer: UByteArray
    public fun update() {
        display.update(buffer)
    }

    public abstract fun setPixel(x: Int, y: Int)
}
