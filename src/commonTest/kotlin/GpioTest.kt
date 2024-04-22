package ch.softappeal.konapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

abstract class GpioTest {
    @Test
    fun errors() {
        val myGpio = Gpio()
        val out = myGpio.output(GPIO_OUT_CONNECTED_TO_IN, false)
        val `in` = myGpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable)
        assertFalse(`in`.get())
        myGpio.close()
        println(assertFails { out.set(false) })
        println(assertFails { `in`.get() })
        Gpio().use { gpio ->
            assertTrue(gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp).get())
            println(assertFails { gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp) })
            println(assertFails { gpio.listen(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp, 1.seconds, Gpio.Edge.Both) { _, _ -> true } })
            println(assertFails { gpio.output(GPIO_IN_UNCONNECTED, false) })
        }
    }

    @Test
    fun active() {
        Gpio().use { gpio ->
            println("active - out: high, in: high")
            gpio.output(GPIO_OUT_CONNECTED_TO_IN, false).use { out ->
                gpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable).use { `in` ->
                    assertFalse(`in`.get())
                    out.set(true)
                    assertTrue(`in`.get())
                }
            }
            gpio.output(GPIO_OUT_CONNECTED_TO_IN, false)
            assertFalse(gpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable).get())
        }
        Gpio().use { gpio ->
            println("active - out: low, in: high")
            val out = gpio.output(GPIO_OUT_CONNECTED_TO_IN, false, Gpio.Active.Low)
            val `in` = gpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable)
            assertTrue(`in`.get())
            out.set(true)
            assertFalse(`in`.get())
        }
        Gpio().use { gpio ->
            println("active - out: high, in: low")
            val out = gpio.output(GPIO_OUT_CONNECTED_TO_IN, false)
            val `in` = gpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable, Gpio.Active.Low)
            assertTrue(`in`.get())
            out.set(true)
            assertFalse(`in`.get())
        }
        Gpio().use { gpio ->
            println("active - out: low, in: low")
            val out = gpio.output(GPIO_OUT_CONNECTED_TO_IN, false, Gpio.Active.Low)
            val `in` = gpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable, Gpio.Active.Low)
            assertFalse(`in`.get())
            out.set(true)
            assertTrue(`in`.get())
        }
    }

    @Test
    fun bias() = runBlocking {
        Gpio().use { gpio ->
            println("bias - PullUp, high")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp)
            delay(10.milliseconds)
            assertTrue(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullDown, high")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullDown)
            delay(10.milliseconds)
            assertFalse(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullUp, Low")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp, Gpio.Active.Low)
            delay(10.milliseconds)
            assertFalse(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullDown, Low")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullDown, Gpio.Active.Low)
            delay(10.milliseconds)
            assertTrue(input.get())
        }
    }

    @Test
    fun listen() = runBlocking {
        repeat(2) { iteration ->
            println("iteration: $iteration")
            Gpio().use { gpio ->
                coroutineScope {
                    val out = gpio.output(GPIO_OUT_CONNECTED_TO_IN, false)
                    delay(100.milliseconds)
                    launch(Dispatchers.IO) {
                        var counter = 0
                        // NOTE: there is an unexpected Falling notification at start of first iteration (on Pi 5 but not on Pi Zero 2 W); why?
                        val timedOut = !gpio.listen(
                            GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable, 200.milliseconds, Gpio.Edge.Both
                        ) { risingEdge, nanoSeconds ->
                            println("notification: $risingEdge ${(nanoSeconds / 1_000_000) % 10_000}")
                            ++counter < 6
                        }
                        println("timedOut: $timedOut")
                    }
                    repeat(2 + iteration) {
                        listOf(true, false).forEach { value ->
                            delay(100.milliseconds)
                            println("out: $value")
                            out.set(value)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun checkDisplayPinsFree() {
        Gpio().use { gpio ->
            gpio.input(GPIO_DISPLAY_DC, Gpio.Bias.Disable)
            gpio.input(GPIO_DISPLAY_RST, Gpio.Bias.Disable)
        }
    }
}
