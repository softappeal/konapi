@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.graphics

import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.Color
import java.awt.Frame
import java.awt.Graphics
import ch.softappeal.kopi.graphics.Color as KopiColor
import ch.softappeal.kopi.graphics.Graphics as KopiGraphics

public class AwtGraphics(display: Display, private val zoom: Int) : KopiGraphics(display) {
    override val buffer: UByteArray = UByteArray(0) // dummy

    private data class Pixel(val x: Int, val y: Int, val color: KopiColor)

    private val pixels = mutableListOf<Pixel>()
    override fun setPixel(x: Int, y: Int) {
        pixels.add(Pixel(x, y, color))
    }

    public fun show() {
        val frame = Frame("AwtGraphics")
        frame.layout = BorderLayout()
        frame.add(object : Canvas() {
            override fun paint(g: Graphics) {
                pixels.forEach { (x, y, color) ->
                    g.color = Color(color.red, color.green, color.blue)
                    g.fillRect(x * zoom, y * zoom, zoom, zoom)
                }
            }
        }, BorderLayout.CENTER)
        frame.setLocation(200, 200)
        frame.isVisible = true
        frame.setSize(width * zoom, height * zoom + frame.insets.top)
    }
}

public fun AwtGraphics(width: Int, height: Int, zoom: Int): AwtGraphics = AwtGraphics(object : Display(width, height) {
    override fun update(buffer: UByteArray): Unit = throw NotImplementedError()
}, zoom)
