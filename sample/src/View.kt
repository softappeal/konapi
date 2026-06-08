package sample

import ch.softappeal.konapi.graphics.BLACK
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.draw
import ch.softappeal.konapi.graphics.fillRect
import ch.softappeal.konapi.graphics.retainColor
import kotlin.time.measureTime

const val filesDir = "sample-files"

fun Graphics.drawLine(line: Int, s: String) {
    draw(0, line * font.height, s)
}

abstract class View(private val graphics: Graphics) {
    protected abstract fun Graphics.drawImpl()
    fun draw() {
        with(graphics) {
            retainColor { set(BLACK).fillRect() }
            drawImpl()
            println("update: ${measureTime { update() }}")
        }
    }

    open fun nextPage() {}
    open fun prevPage() {}
}
