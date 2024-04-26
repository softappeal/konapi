package ch.softappeal.konapi.devices

import ch.softappeal.konapi.GPIO_DISPLAY_DC
import ch.softappeal.konapi.GPIO_DISPLAY_RST
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.devices.waveshare.Oled
import ch.softappeal.konapi.devices.waveshare.bwOled1in3
import ch.softappeal.konapi.devices.waveshare.color16Oled1in5
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.testDisplay
import ch.softappeal.konapi.sleepMs
import ch.softappeal.konapi.spiDeviceBus0CS0
import ch.softappeal.konapi.spiDeviceBus0CS1
import ch.softappeal.konapi.use
import kotlin.test.Test

private fun <G : Graphics> Oled<G>.test() = use { display ->
    display.graphics.testDisplay()
    sleepMs(5000)
}

abstract class OledTest {
    @Test
    fun color16Oled1in5() {
        spiDeviceBus0CS0().use { device ->
            Gpio().use { gpio ->
                color16Oled1in5(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device).test()
            }
        }
    }

    @Test
    fun bwOled1in3() {
        spiDeviceBus0CS1().use { device ->
            Gpio().use { gpio ->
                bwOled1in3(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device).test()
            }
        }
    }
}
