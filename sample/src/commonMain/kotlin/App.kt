@file:Suppress("unused")

package sample

import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.devices.bosch.Bme280
import ch.softappeal.konapi.devices.waveshare.Paj7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2.Gesture
import ch.softappeal.konapi.devices.waveshare.bwOled1in3
import ch.softappeal.konapi.devices.waveshare.color16Oled1in5
import ch.softappeal.konapi.graphics.Graphics
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

fun main() {
    i2cBus1().use { bus ->
        val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
        val bme280 = Bme280(bus.device(I2C_ADDRESS_BME280))
        Gpio().use { gpio ->
            // bwDisplay(gpio) { graphics ->
            colorDisplay(gpio) { graphics ->
                with(Display(graphics)) {
                    gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, INFINITE, Gpio.Edge.Falling) { _, _ ->
                        println(bme280.measurement())
                        val gesture = paj7620U2.gesture()
                        println("gesture: $gesture")
                        when (gesture) {
                            Gesture.Up -> prevFont()
                            Gesture.Down -> nextFont()
                            Gesture.Left -> nextColor()
                            Gesture.Right -> prevColor()
                            else -> {}
                        }
                        true
                    }
                }
            }
        }
    }
}
