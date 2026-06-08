@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

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
public open class Dimension(public val width: Int, public val height: Int) {
    public constructor(dimension: Dimension) : this(dimension.width, dimension.height)

    init {
        require(width > 0) { "width=$width must be > 0" }
        require(height > 0) { "height=$height must be > 0" }
    }
}

public val PRIMARY_COLOR_RANGE: IntRange = 0..255

public data class Color(public val red: Int, public val green: Int, public val blue: Int) {
    init {
        require(red in PRIMARY_COLOR_RANGE) { "red=$red must be in $PRIMARY_COLOR_RANGE" }
        require(green in PRIMARY_COLOR_RANGE) { "green=$green must be in $PRIMARY_COLOR_RANGE" }
        require(blue in PRIMARY_COLOR_RANGE) { "blue=$blue must be in $PRIMARY_COLOR_RANGE" }
    }
}

public val BLACK: Color = Color(0x00, 0x00, 0x00)
public val WHITE: Color = Color(0xFF, 0xFF, 0xFF)
public val RED: Color = Color(0xFF, 0x00, 0x00)
public val GREEN: Color = Color(0x00, 0xFF, 0x00)
public val BLUE: Color = Color(0x00, 0x00, 0xFF)
public val CYAN: Color = Color(0x00, 0xFF, 0xFF)
public val MAGENTA: Color = Color(0xFF, 0x00, 0xFF)
public val YELLOW: Color = Color(0xFF, 0xFF, 0x00)

public abstract class Display(width: Int, height: Int) : Dimension(width, height) {
    public abstract fun update(buffer: UByteArray)
}

private val NO_FONT = Overlay(0, Dimension(1, 1), byteArrayOf())

public abstract class Graphics(private val display: Display) : Dimension(display) {
    public var color: Color = BLACK
        set(value) {
            field = value
            setColorImpl()
        }

    protected abstract fun setColorImpl()

    public fun set(color: Color): Graphics {
        this.color = color
        return this
    }

    public var font: Overlay = NO_FONT
    public fun set(font: Overlay): Graphics {
        this.font = font
        return this
    }

    protected abstract val buffer: UByteArray
    public fun update() {
        display.update(buffer)
    }

    public fun setPixel(x: Int, y: Int) {
        require(x in 0..<width) { "x=$x must be in 0..<$width" }
        require(y in 0..<height) { "y=$y must be in 0..<$height" }
        setPixelImpl(x, y)
    }

    protected abstract fun setPixelImpl(x: Int, y: Int)
}

public inline fun <R> Graphics.retainColor(action: Graphics.() -> R): R {
    val retained = color
    return try {
        action()
    } finally {
        color = retained
    }
}
