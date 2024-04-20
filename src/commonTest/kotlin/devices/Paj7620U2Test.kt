package ch.softappeal.konapi.devices

import ch.softappeal.konapi.GPIO_PAJ7620U2_INT
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.I2C_ADDRESS_PAJ7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2
import ch.softappeal.konapi.devices.waveshare.Paj7620U2.Gesture
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

abstract class Paj7620U2Test {
    @Test
    fun test() {
        runBlocking {
            i2cBus1().use { bus ->
                Gpio().use { gpio ->
                    coroutineScope {
                        val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
                        val flow =
                            MutableSharedFlow<Gesture?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                        val job = launch {
                            flow.collect { println(it) }
                        }
                        println("gesture: ${paj7620U2.gesture()}")
                        println("make all 9 gestures ...")
                        launch(Dispatchers.IO) {
                            gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, 5.seconds) { edge, _ ->
                                if (edge == Gpio.Edge.Falling) flow.emit(paj7620U2.gesture())
                                true
                            }
                            job.cancel()
                        }
                    }
                }
            }
        }
    }
}
