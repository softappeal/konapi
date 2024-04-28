package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.readFile

private fun testDisplay() {
    AwtGraphics(4, Dimension(128, 64)).apply {
        testDisplay()
    }.showWindow(100, 200)
}

private fun showRawImage() {
    AwtGraphics(4, Dimension(150, 150)).apply {
        set(WHITE).fillRect()
        draw(10, 10, RawImage(128, 128, readFile("test-files/me.128x128.rgb.raw")))
    }.showWindow(700, 200)
}

fun main() {
    testDisplay()
    showRawImage()
}
