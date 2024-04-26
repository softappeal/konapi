package ch.softappeal.konapi.graphics

import kotlin.time.measureTime

fun Graphics.testDisplay() {
    set(BLACK).fillRect()
    var x = 0
    val colors = listOf(RED, GREEN, BLUE, BLACK, WHITE, CYAN, MAGENTA, YELLOW)
    val stripes = 8
    val w = width / colors.size / stripes
    colors.forEach { c ->
        repeat(stripes) { s ->
            fun map(color: Int) = color / (s + 1)
            color = Color(map(c.red), map(c.green), map(c.blue))
            fillRect(x, 0, w, height - (s * 5))
            x += w
        }
    }
    println("update: ${measureTime { update() }}")
}
