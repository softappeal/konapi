package ch.softappeal.konapi

import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

abstract class GpioTest {
    @Test
    fun errors() {
        val myGpio = Gpio()
        val out = myGpio.output(GPIO_OUT_CONNECTED_TO_IN, false)
        val `in` = myGpio.input(GPIO_IN_CONNECTED_TO_OUT, Gpio.Bias.Disable)
        assertFalse(`in`.get())
        myGpio.close()
        assertFails { out.set(false) }
        assertFails { `in`.get() }
        Gpio().use { gpio ->
            assertTrue(gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp).get())
            assertFails { gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp) }
            assertFails { gpio.listen(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp, 1.seconds, Gpio.Edge.Both) { _, _ -> true } }
            assertFails { gpio.output(GPIO_IN_UNCONNECTED, false) }
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
    fun bias() {
        Gpio().use { gpio ->
            println("bias - PullUp, high")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp)
            sleepMs(10)
            assertTrue(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullDown, high")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullDown)
            sleepMs(10)
            assertFalse(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullUp, Low")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullUp, Gpio.Active.Low)
            sleepMs(10)
            assertFalse(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullDown, Low")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Gpio.Bias.PullDown, Gpio.Active.Low)
            sleepMs(10)
            assertTrue(input.get())
        }
    }
}
