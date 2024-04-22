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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

private fun <G : Graphics> displayCreator(oledCreator: suspend () -> Oled<G>) = object : DisplayCreator {
    var oled: Oled<G>? = null

    override suspend fun create() {
        oled = oledCreator()
    }

    override fun close() {
        oled!!.close()
    }

    override val graphics get() = oled!!.graphics
}

private suspend fun Displays.test(gpio: Gpio, paj7620U2: Paj7620U2) = coroutineScope {
    val flow = MutableSharedFlow<Gesture?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val job = launch {
        flow.collect { gesture ->
            when (gesture) {
                null, Gesture.Wave -> {}
                Gesture.Up -> prevFont()
                Gesture.Down -> nextFont()
                Gesture.Left -> prevPage()
                Gesture.Right -> nextPage()
                Gesture.Forward -> nextColor()
                Gesture.Backward -> prevColor()
                Gesture.Clockwise -> nextDisplay()
                Gesture.AntiClockwise -> prevDisplay()
            }
        }
    }
    launch(Dispatchers.IO) {
        gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, 20.seconds, Gpio.Edge.Falling) { _, _ ->
            val gesture = paj7620U2.gesture()
            println("gesture: $gesture")
            assertTrue(flow.tryEmit(gesture))
            true
        }
        job.cancel()
    }
}

abstract class OledFontTest {
    @Test
    fun test(): Unit = runBlocking {
        Gpio().use { gpio ->
            spiDeviceBus0CS0().use { device0 ->
                spiDeviceBus0CS1().use { device1 ->
                    i2cBus1().use { bus ->
                        Displays(listOf(
                            displayCreator { color16Oled1in5(device0, gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST) },
                            displayCreator { bwOled1in3(null, device1, gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST) },
                        )).use { displays ->
                            displays.init()
                            displays.test(gpio, Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2)))
                        }
                    }
                }
            }
        }
    }
}
