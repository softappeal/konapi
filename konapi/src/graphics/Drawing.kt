package ch.softappeal.konapi.graphics

public fun Graphics.setPixel(point: Point) {
    setPixel(point.x, point.y)
}

public fun Graphics.fillRect(xTopLeft: Int, yTopLeft: Int, width: Int, height: Int) {
    for (x in xTopLeft..<xTopLeft + width) {
        for (y in yTopLeft..<yTopLeft + height) {
            setPixel(x, y)
        }
    }
}

public fun Graphics.fillRect(topLeft: Point, dimension: Dimension) {
    fillRect(topLeft.x, topLeft.y, dimension.width, dimension.height)
}

public fun Graphics.fillRect() {
    fillRect(0, 0, width, height)
}

public class Rect(public val xTopLeft: Int, public val yTopLeft: Int, width: Int, height: Int) : Dimension(width, height) {
    public constructor(topLeft: Point, dimension: Dimension) : this(topLeft.x, topLeft.y, dimension.width, dimension.height)

    public val topLeft: Point get() = Point(xTopLeft, yTopLeft)
}

public fun Graphics.fillRect(rect: Rect) {
    with(rect) { fillRect(xTopLeft, yTopLeft, width, height) }
}
