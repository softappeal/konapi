package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.readFile

fun main() {
    val image = RawImage(Dimensions(128, 128), readFile("test.files/me.128x128.rgb.raw"))
    AwtGraphics(location = Point(100, 200), Dimensions(200, 200), zoom = 4).apply {
        draw(20, 50, image)
    }.show()
}
