package ch.softappeal.konapi.devices

import ch.softappeal.konapi.GPIO_DISPLAY_DC
import ch.softappeal.konapi.GPIO_DISPLAY_RST
import ch.softappeal.konapi.GPIO_PAJ7620U2_INT
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.konapi.devices.waveshare.Oled
import ch.softappeal.konapi.devices.waveshare.Paj7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2.Gesture
import ch.softappeal.konapi.devices.waveshare.bwOled1in3
import ch.softappeal.konapi.devices.waveshare.color16Oled1in5
import ch.softappeal.konapi.graphics.DisplayCreator
import ch.softappeal.konapi.graphics.Displays
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.spiDeviceBus0CS0
import ch.softappeal.konapi.spiDeviceBus0CS1
import ch.softappeal.konapi.use
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

private fun <G : Graphics> displayCreator(oledCreator: suspend () -> Oled<G>) = object : DisplayCreator {
    var oled: Oled<G>? = null

    override suspend fun create() {
        oled = oledCreator()
    }

    override suspend fun close() {
        oled!!.close()
    }

    override val graphics get() = oled!!.graphics
}

abstract class OledFontTest {
    @Test
    fun test(): Unit = runBlocking {
        Gpio().use { gpio ->
            spiDeviceBus0CS0().use { device0 ->
                spiDeviceBus0CS1().use { device1 ->
                    i2cBus1().use { bus ->
                        Displays(listOf(
                            displayCreator { color16Oled1in5(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device0) },
                            displayCreator { bwOled1in3(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device1) },
                        )).use { displays ->
                            displays.init()
                            val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
                            gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, 10.seconds, Gpio.Edge.Falling) { _, _ ->
                                val gesture = paj7620U2.gesture()
                                println("gesture: $gesture")
                                when (gesture) {
                                    null, Gesture.Wave -> {}
                                    Gesture.Up -> displays.prevFont()
                                    Gesture.Down -> displays.nextFont()
                                    Gesture.Left -> displays.prevPage()
                                    Gesture.Right -> displays.nextPage()
                                    Gesture.Forward -> displays.nextColor()
                                    Gesture.Backward -> displays.prevColor()
                                    Gesture.Clockwise -> runBlocking { displays.nextDisplay() }
                                    Gesture.AntiClockwise -> runBlocking { displays.prevDisplay() }
                                }
                                true
                            }
                        }
                    }
                }
            }
        }
    }
}
