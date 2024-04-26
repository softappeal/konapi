@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.tryFinally
import java.awt.BorderLayout
import java.awt.Canvas
import java.awt.Color
import java.awt.Frame
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import ch.softappeal.konapi.graphics.Graphics as KGraphics

public fun <R> drawImage(width: Int, height: Int, draw: Graphics.(image: BufferedImage) -> R): R {
    val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)
    val graphics = image.createGraphics()
    return tryFinally({
        graphics.draw(image)
    }) {
        graphics.dispose()
    }
}

public class AwtGraphics(private val zoom: Int, display: Display) : KGraphics(display) {
    init {
        require(zoom > 0) { "zoom=$zoom must be > 0" }
    }

    override val buffer: UByteArray = UByteArray(width * height * 3)
    private fun index(x: Int, y: Int) = (x + y * width) * 3

    override fun setColorImpl() {} // empty

    override fun setPixelImpl(x: Int, y: Int) {
        val b = index(x, y)
        buffer[b] = color.red.toUByte()
        buffer[b + 1] = color.green.toUByte()
        buffer[b + 2] = color.blue.toUByte()
    }

    private fun Graphics.draw() {
        for (x in 0..<width) {
            for (y in 0..<height) {
                val b = index(x, y)
                color = Color(buffer[b].toInt(), buffer[b + 1].toInt(), buffer[b + 2].toInt())
                fillRect(x * zoom, y * zoom, zoom, zoom)
            }
        }
    }

    public fun showWindow(xLocation: Int, yLocation: Int) {
        val frame = Frame("AwtGraphics")
        frame.layout = BorderLayout()
        frame.add(object : Canvas() {
            override fun paint(g: Graphics) {
                g.draw()
            }
        }, BorderLayout.CENTER)
        frame.setLocation(xLocation, yLocation)
        frame.isVisible = true
        frame.setSize(width * zoom, height * zoom + frame.insets.top)
    }

    public fun writePng(path: String) {
        drawImage(width * zoom, height * zoom) { image ->
            draw()
            ImageIO.write(image, "png", File(path))
        }
    }
}

public fun AwtGraphics(zoom: Int, dimensions: Dimensions): AwtGraphics =
    AwtGraphics(zoom, object : Display(dimensions.width, dimensions.height) {
        override fun update(buffer: UByteArray) {} // empty
    })
