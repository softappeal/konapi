@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

/** Pixel is right of [x] and below of [y]. */
public data class Point(public val x: Int, public val y: Int)

/**
 * [Point] (0         , 0           ) is top    left.
 *
 * [Point] ([width] -1, 0           ) is top    right.
 *
 * [Point] (0         , [height] - 1) is bottom left.
 *
 * [Point] ([width] -1, [height] - 1) is bottom right.
 */
public data class Dimensions(public val width: Int, public val height: Int)

public abstract class HasDimensions(public val width: Int, public val height: Int)

public data class Color(public val red: Int, public val green: Int, public val blue: Int)

public val BLACK: Color = Color(0x00, 0x00, 0x00)
public val WHITE: Color = Color(0xFF, 0xFF, 0xFF)
public val RED: Color = Color(0xFF, 0x00, 0x00)
public val GREEN: Color = Color(0x00, 0xFF, 0x00)
public val BLUE: Color = Color(0x00, 0x00, 0xFF)
public val CYAN: Color = Color(0x00, 0xFF, 0xFF)
public val MAGENTA: Color = Color(0xFF, 0x00, 0xFF)
public val YELLOW: Color = Color(0xFF, 0xFF, 0x00)

public abstract class Display(width: Int, height: Int) : HasDimensions(width, height) {
    public abstract fun update(buffer: UByteArray)
}

public abstract class Graphics(private val display: Display) : HasDimensions(display.width, display.height) {
    private var _color: Color? = null
    public val color: Color get() = _color!!
    public open fun set(color: Color): Graphics {
        _color = color
        return this
    }

    protected abstract val buffer: UByteArray
    public fun update() {
        display.update(buffer)
    }

    public abstract fun setPixel(x: Int, y: Int)
}
