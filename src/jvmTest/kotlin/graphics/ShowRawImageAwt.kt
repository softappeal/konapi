package ch.softappeal.konapi.graphics

fun main() {
    AwtGraphics(4, Dimensions(200, 200)).apply {
        set(WHITE).fillRect()
        draw(20, 50, imageOfMe)
    }.showWindow(100, 200)
}
