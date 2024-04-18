package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.readFile

fun main() {
    val image = RawImage(128, 128, readFile("test.files/me.128x128.rgb.raw"))
    AwtGraphics(200, 200).apply {
        set(WHITE).fillRect()
        draw(20, 50, image)
    }.showWindow(Point(100, 200), 4)
}
