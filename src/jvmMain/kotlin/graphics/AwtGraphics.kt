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

public fun drawImage(width: Int, height: Int, draw: Graphics.() -> Unit): BufferedImage {
    val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)
    val graphics = image.createGraphics()
    tryFinally({
        graphics.draw()
    }) {
        graphics.dispose()
    }
    return image
}

public class AwtGraphics(display: Display) : KGraphics(display) {
    override val buffer: UByteArray = UByteArray(width * height * 3)
    private fun index(x: Int, y: Int) = (x + y * width) * 3

    override fun setPixel(x: Int, y: Int) {
        val b = index(x, y)
        buffer[b] = color.red.toUByte()
        buffer[b + 1] = color.green.toUByte()
        buffer[b + 2] = color.blue.toUByte()
    }

    private fun Graphics.draw(zoom: Int) {
        for (x in 0..<width) {
            for (y in 0..<height) {
                val b = index(x, y)
                color = Color(buffer[b].toInt(), buffer[b + 1].toInt(), buffer[b + 2].toInt())
                fillRect(x * zoom, y * zoom, zoom, zoom)
            }
        }
    }

    public fun showWindow(location: Point, zoom: Int) {
        val frame = Frame("AwtGraphics")
        frame.layout = BorderLayout()
        frame.add(object : Canvas() {
            override fun paint(g: Graphics) {
                g.draw(zoom)
            }
        }, BorderLayout.CENTER)
        frame.setLocation(location.x, location.y)
        frame.isVisible = true
        frame.setSize(width * zoom, height * zoom + frame.insets.top)
    }

    public fun writePng(path: String) {
        ImageIO.write(drawImage(width, height) { draw(1) }, "png", File(path))
    }
}

public fun AwtGraphics(width: Int, height: Int): AwtGraphics = AwtGraphics(object : Display(width, height) {
    override fun update(buffer: UByteArray): Unit = throw NotImplementedError()
})
