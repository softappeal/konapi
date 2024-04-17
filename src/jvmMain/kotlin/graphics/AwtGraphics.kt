@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.Color
import java.awt.Frame
import java.awt.Graphics
import ch.softappeal.konapi.graphics.Color as KColor
import ch.softappeal.konapi.graphics.Graphics as KGraphics

public class AwtGraphics(private val location: Point, display: Display, private val zoom: Int) : KGraphics(display) {
    override val buffer: UByteArray = UByteArray(0) // dummy

    private data class Pixel(val x: Int, val y: Int, val color: KColor)

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
        frame.setLocation(location.x, location.y)
        frame.isVisible = true
        frame.setSize(width * zoom, height * zoom + frame.insets.top)
    }
}

public fun AwtGraphics(location: Point, dimensions: Dimensions, zoom: Int): AwtGraphics = AwtGraphics(
    location,
    object : Display(dimensions.width, dimensions.height) {
        override fun update(buffer: UByteArray): Unit = throw NotImplementedError()
    },
    zoom,
)
