package ch.softappeal.kopi.gpio

import ch.softappeal.kopi.GPIO_IN_CONNECTED_TO_OUT
import ch.softappeal.kopi.GPIO_IN_UNCONNECTED
import ch.softappeal.kopi.GPIO_OUT_CONNECTED_TO_IN
import ch.softappeal.kopi.gpio.Gpio.Active
import ch.softappeal.kopi.gpio.Gpio.Bias
import ch.softappeal.kopi.printlnCC
import ch.softappeal.kopi.use
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
    fun errors() = runBlocking {
        val myChip = Gpio()
        val out = myChip.output(GPIO_OUT_CONNECTED_TO_IN, false)
        val `in` = myChip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable)
        assertFalse(`in`.get())
        myChip.close()
        println(assertFails { out.set(false) })
        println(assertFails { `in`.get() })

        val chip1 = Gpio()
        val chip2 = Gpio() // opening the same chip twice seems to be legal
        chip2.close()
        chip1.close()

        Gpio().use { chip ->
            assertTrue(chip.input(GPIO_IN_UNCONNECTED, Bias.PullUp).get())
            println(assertFails { chip.input(GPIO_IN_UNCONNECTED, Bias.PullUp) })
            println(assertFails { chip.listen(GPIO_IN_UNCONNECTED, Bias.PullUp, 1.seconds) { _, _ -> true } })
            println(assertFails { chip.output(GPIO_IN_UNCONNECTED, false) })
        }
    }

    @Test
    fun active() {
        Gpio().use { chip ->
            println("active - out: high, in: high")
            chip.output(GPIO_OUT_CONNECTED_TO_IN, false).use { out ->
                chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable).use { `in` ->
                    assertFalse(`in`.get())
                    out.set(true)
                    assertTrue(`in`.get())
                }
            }
            chip.output(GPIO_OUT_CONNECTED_TO_IN, false)
            assertFalse(chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable).get())
        }
        Gpio().use { chip ->
            println("active - out: low, in: high")
            val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false, Active.Low)
            val `in` = chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable)
            assertTrue(`in`.get())
            out.set(true)
            assertFalse(`in`.get())
        }
        Gpio().use { chip ->
            println("active - out: high, in: low")
            val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false)
            val `in` = chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.Low)
            assertTrue(`in`.get())
            out.set(true)
            assertFalse(`in`.get())
        }
        Gpio().use { chip ->
            println("active - out: low, in: low")
            val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false, Active.Low)
            val `in` = chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.Low)
            assertFalse(`in`.get())
            out.set(true)
            assertTrue(`in`.get())
        }
    }

    @Test
    fun bias() = runBlocking {
        Gpio().use { gpio ->
            println("bias - PullUp, high")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullUp)
            delay(10.milliseconds)
            assertTrue(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullDown, high")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullDown)
            delay(10.milliseconds)
            assertFalse(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullUp, Low")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullUp, Active.Low)
            delay(10.milliseconds)
            assertFalse(input.get())
        }
        Gpio().use { gpio ->
            println("bias - PullDown, Low")
            val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullDown, Active.Low)
            delay(10.milliseconds)
            assertTrue(input.get())
        }
    }

    @Test
    fun listen() = runBlocking {
        repeat(2) { iteration ->
            printlnCC("iteration: $iteration")
            Gpio().use { chip ->
                coroutineScope {
                    printlnCC("coroutineScope")
                    val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false)
                    delay(100.milliseconds)
                    launch(Dispatchers.IO) {
                        printlnCC("launch")
                        var counter = 0
                        val timedOut = !chip.listen(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, 200.milliseconds) { edge, nanoSeconds ->
                            printlnCC("notification: $edge ${(nanoSeconds / 1_000_000) % 10_000}")
                            ++counter < 6
                        }
                        printlnCC("timedOut: $timedOut")
                    }
                    repeat(2 + iteration) {
                        listOf(true, false).forEach { value ->
                            delay(100.milliseconds)
                            printlnCC("out: $value")
                            out.set(value)
                        }
                    }
                }
            }
        }
    }
}
