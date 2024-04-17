package ch.softappeal.konapi.graphics

/** [bytes] is (red, green, blue) x width x height. */
public class RawImage(dimensions: Dimensions, internal val bytes: ByteArray) : Dimensions(dimensions)

public fun Graphics.draw(xTopLeft: Int, yTopLeft: Int, image: RawImage) {
    val bytes = image.bytes
    var b = 0
    for (y in yTopLeft..<yTopLeft + image.height) {
        for (x in xTopLeft..<xTopLeft + image.width) {
            fun color() = bytes[b++].toUByte().toInt()
            set(Color(color(), color(), color())).setPixel(x, y)
        }
    }
}

public fun Graphics.draw(topLeft: Point, image: RawImage) {
    draw(topLeft.x, topLeft.y, image)
}
