package ch.softappeal.konapi.graphics.tools

import ch.softappeal.konapi.graphics.AwtGraphics
import ch.softappeal.konapi.graphics.Point

fun main() {
    showFont { dimensions -> AwtGraphics(location = Point(100, 200), dimensions, zoom = 4) }.show()
}
