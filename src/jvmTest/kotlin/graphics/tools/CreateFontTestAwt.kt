package ch.softappeal.kopi.graphics.tools

import ch.softappeal.kopi.graphics.AwtGraphics

fun main() {
    showFont { width, height -> AwtGraphics(width, height, 4) }.show()
}
