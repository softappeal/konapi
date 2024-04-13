package ch.softappeal.kopi.graphics

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

public fun Graphics.fillRect(topLeft: Point, dimensions: Dimensions) {
    fillRect(topLeft.x, topLeft.y, dimensions.width, dimensions.height)
}

public fun Graphics.fillRect() {
    fillRect(0, 0, width, height)
}
