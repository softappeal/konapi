package ch.softappeal.konapi.graphics

/** [bytes] is (red, green, blue) x width x height. */
public class RawImage(width: Int, height: Int, internal val bytes: ByteArray) : Dimension(width, height) {
    init {
        require(bytes.size == width * height * 3) { "bytes.size=${bytes.size} must be ${width * height * 3}" }
    }
}

public fun Graphics.draw(xTopLeft: Int, yTopLeft: Int, image: RawImage): Unit = retainColor {
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
