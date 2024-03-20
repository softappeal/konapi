package ch.softappeal.kopi.test.gpio

import ch.softappeal.kopi.app.GPIO_IN_CONNECTED_TO_OUT
import ch.softappeal.kopi.app.GPIO_IN_UNCONNECTED
import ch.softappeal.kopi.app.GPIO_OUT_CONNECTED_TO_IN
import ch.softappeal.kopi.lib.gpio.Gpio
import ch.softappeal.kopi.lib.gpio.Gpio.Active
import ch.softappeal.kopi.lib.gpio.Gpio.Bias
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private suspend fun errors(label: String) {
    class MyGpio : Gpio(label) {
        val out = output(GPIO_OUT_CONNECTED_TO_IN, false)
        val `in` = input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable)
    }

    val myChip = MyGpio()
    assertFalse(myChip.`in`.get())
    myChip.close()
    println(assertFails {
        myChip.out.set(false)
    })
    println(assertFails {
        myChip.`in`.get()
    })

    val chip1 = Gpio(label)
    val chip2 = Gpio(label) // opening the same chip twice seems to be legal
    chip2.close()
    chip1.close()

    Gpio(label).use { chip ->
        assertTrue(chip.input(GPIO_IN_UNCONNECTED, Bias.PullUp).get())
        println(assertFails {
            chip.input(GPIO_IN_UNCONNECTED, Bias.PullUp)
        })
        println(assertFails {
            chip.listen(GPIO_IN_UNCONNECTED, Bias.PullUp, 1.seconds) { _, _ -> true }
        })
        println(assertFails {
            chip.output(GPIO_IN_UNCONNECTED, false)
        })
    }
}

private fun active(label: String) {
    Gpio(label).use { chip ->
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
    Gpio(label).use { chip ->
        println("active - out: low, in: high")
        val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false, Active.Low)
        val `in` = chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable)
        assertTrue(`in`.get())
        out.set(true)
        assertFalse(`in`.get())
    }
    Gpio(label).use { chip ->
        println("active - out: high, in: low")
        val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false)
        val `in` = chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.Low)
        assertTrue(`in`.get())
        out.set(true)
        assertFalse(`in`.get())
    }
    Gpio(label).use { chip ->
        println("active - out: low, in: low")
        val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false, Active.Low)
        val `in` = chip.input(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.Low)
        assertFalse(`in`.get())
        out.set(true)
        assertTrue(`in`.get())
    }
}

private suspend fun bias(label: String) {
    Gpio(label).use { gpio ->
        println("bias - PullUp, high")
        val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullUp)
        delay(10.milliseconds)
        assertTrue(input.get())
    }
    Gpio(label).use { gpio ->
        println("bias - PullDown, high")
        val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullDown)
        delay(10.milliseconds)
        assertFalse(input.get())
    }
    Gpio(label).use { gpio ->
        println("bias - PullUp, Low")
        val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullUp, Active.Low)
        delay(10.milliseconds)
        assertFalse(input.get())
    }
    Gpio(label).use { gpio ->
        println("bias - PullDown, Low")
        val input = gpio.input(GPIO_IN_UNCONNECTED, Bias.PullDown, Active.Low)
        delay(10.milliseconds)
        assertTrue(input.get())
    }
}

private suspend fun listen(label: String) {
    repeat(2) { iteration ->
        println("iteration: $iteration")
        Gpio(label).use { chip ->
            coroutineScope {
                val out = chip.output(GPIO_OUT_CONNECTED_TO_IN, false)
                delay(100.milliseconds)
                launch(Dispatchers.Default) {
                    var counter = 0
                    val timedOut = !chip.listen(GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, 200.milliseconds) { edge, nanoSeconds ->
                        println("notification: $edge ${(nanoSeconds / 1_000_000) % 10_000}")
                        ++counter < 6
                    }
                    println("timedOut: $timedOut")
                }
                repeat(2 + iteration) {
                    delay(100.milliseconds)
                    println("out: true")
                    out.set(true)
                    delay(100.milliseconds)
                    println("out: false")
                    out.set(false)
                }
            }
        }
    }
}

public suspend fun gpioTest(label: String) {
    println("gpioTest")
    errors(label)
    active(label)
    bias(label)
    listen(label)
}
