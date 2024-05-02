@file:Suppress("unused")

package sample

import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.devices.bosch.Bme280
import ch.softappeal.konapi.devices.waveshare.Paj7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2.Gesture
import ch.softappeal.konapi.devices.waveshare.bwOled1in3
import ch.softappeal.konapi.devices.waveshare.color16Oled1in5
import ch.softappeal.konapi.graphics.BLUE
import ch.softappeal.konapi.graphics.CYAN
import ch.softappeal.konapi.graphics.GREEN
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.MAGENTA
import ch.softappeal.konapi.graphics.RED
import ch.softappeal.konapi.graphics.WHITE
import ch.softappeal.konapi.graphics.YELLOW
import ch.softappeal.konapi.use
import kotlin.time.Duration.Companion.INFINITE

private fun colorDisplay(gpio: Gpio, action: (graphics: Graphics) -> Unit) {
    colorDisplay().use { device ->
        color16Oled1in5(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device).use { action(it.graphics) }
    }
}

private fun bwDisplay(gpio: Gpio, action: (graphics: Graphics) -> Unit) {
    bwDisplay().use { device ->
        bwOled1in3(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device).use { action(it.graphics) }
    }
}

private val colors = listOf(WHITE, RED, GREEN, BLUE, CYAN, MAGENTA, YELLOW)

private fun processGestures(views: List<View>, graphics: Graphics, gpio: Gpio, paj7620U2: Paj7620U2) {
    var viewIndex = 0
    fun view() = views[viewIndex]
    var colorIndex = 0
    fun draw() {
        graphics.set(colors[colorIndex])
        view().draw()
    }
    draw()
    gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, INFINITE, Gpio.Edge.Falling) { _, _ ->
        val gesture = paj7620U2.gesture()
        println("gesture: $gesture")
        when (gesture) {
            Gesture.AntiClockwise -> if (--viewIndex < 0) viewIndex = views.size - 1
            Gesture.Clockwise -> if (++viewIndex >= views.size) viewIndex = 0
            Gesture.Up -> if (--colorIndex < 0) colorIndex = colors.size - 1
            Gesture.Down -> if (++colorIndex >= colors.size) colorIndex = 0
            Gesture.Left -> view().prevPage()
            Gesture.Right -> view().nextPage()
            else -> {}
        }
        draw()
        true
    }
}

fun main() {
    i2cBus1().use { bus ->
        val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
        val bme280 = Bme280(bus.device(I2C_ADDRESS_BME280))
        Gpio().use { gpio ->
            // bwDisplay(gpio) { graphics ->
            colorDisplay(gpio) { graphics ->
                val views = listOf(HelpView(graphics), FontView(graphics), IconView(graphics), Bme280View(graphics, bme280))
                processGestures(views, graphics, gpio, paj7620U2)
            }
        }
    }
}
