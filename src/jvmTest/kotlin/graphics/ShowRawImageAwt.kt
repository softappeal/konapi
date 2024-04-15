package ch.softappeal.kopi.graphics

import ch.softappeal.kopi.readFile

fun main() {
    val image = RawImage(128, 128, readFile("test-files/me.128x128.rgb.raw"))
    AwtGraphics(location = Point(100, 200), Dimensions(200, 200), zoom = 4).apply {
        draw(20, 50, image)
    }.show()
}
