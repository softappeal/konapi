package ch.softappeal.kopi.graphics.tools

import ch.softappeal.kopi.graphics.AwtGraphics
import ch.softappeal.kopi.graphics.Point

fun main() {
    showFont { dimensions -> AwtGraphics(location = Point(100, 200), dimensions, zoom = 4) }.show()
}
