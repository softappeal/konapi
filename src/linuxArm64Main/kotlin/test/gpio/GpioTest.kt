package ch.softappeal.kopi.test.gpio

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

private const val OPEN_IN = 27 // not connected
private const val IN = 22 // NOTE: connected to OUT
private const val OUT = 17

private suspend fun errors(label: String) {
    class MyGpio : Gpio(label) {
        val out = output(OUT, false)
        val `in` = input(IN, Bias.Disable)
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
        assertTrue(chip.input(OPEN_IN, Bias.PullUp).get())
        println(assertFails {
            chip.input(OPEN_IN, Bias.PullUp)
        })
        println(assertFails {
            chip.listen(OPEN_IN, Bias.PullUp, 1.seconds) { _, _ -> true }
        })
        println(assertFails {
            chip.output(OPEN_IN, false)
        })
    }
}

private fun active(label: String) {
    Gpio(label).use { chip ->
        println("active - out: high, in: high")
        chip.output(OUT, false).use { out ->
            chip.input(IN, Bias.Disable).use { `in` ->
                assertFalse(`in`.get())
                out.set(true)
                assertTrue(`in`.get())
            }
        }
        chip.output(OUT, false)
        assertFalse(chip.input(IN, Bias.Disable).get())
    }
    Gpio(label).use { chip ->
        println("active - out: low, in: high")
        val out = chip.output(OUT, false, Active.Low)
        val `in` = chip.input(IN, Bias.Disable)
        assertTrue(`in`.get())
        out.set(true)
        assertFalse(`in`.get())
    }
    Gpio(label).use { chip ->
        println("active - out: high, in: low")
        val out = chip.output(OUT, false)
        val `in` = chip.input(IN, Bias.Disable, Active.Low)
        assertTrue(`in`.get())
        out.set(true)
        assertFalse(`in`.get())
    }
    Gpio(label).use { chip ->
        println("active - out: low, in: low")
        val out = chip.output(OUT, false, Active.Low)
        val `in` = chip.input(IN, Bias.Disable, Active.Low)
        assertFalse(`in`.get())
        out.set(true)
        assertTrue(`in`.get())
    }
}

private suspend fun bias(label: String) {
    Gpio(label).use { gpio ->
        println("bias - PullUp, high")
        val input = gpio.input(OPEN_IN, Bias.PullUp)
        delay(10.milliseconds)
        assertTrue(input.get())
    }
    Gpio(label).use { gpio ->
        println("bias - PullDown, high")
        val input = gpio.input(OPEN_IN, Bias.PullDown)
        delay(10.milliseconds)
        assertFalse(input.get())
    }
    Gpio(label).use { gpio ->
        println("bias - PullUp, Low")
        val input = gpio.input(OPEN_IN, Bias.PullUp, Active.Low)
        delay(10.milliseconds)
        assertFalse(input.get())
    }
    Gpio(label).use { gpio ->
        println("bias - PullDown, Low")
        val input = gpio.input(OPEN_IN, Bias.PullDown, Active.Low)
        delay(10.milliseconds)
        assertTrue(input.get())
    }
}

private suspend fun listen(label: String) {
    repeat(2) { iteration ->
        println("iteration: $iteration")
        Gpio(label).use { chip ->
            coroutineScope {
                val out = chip.output(OUT, false)
                delay(100.milliseconds)
                launch(Dispatchers.Default) {
                    var counter = 0
                    // TODO: there is an unexpected Falling notification at start of first iteration; why?
                    val notTimeout = chip.listen(IN, Bias.Disable, 200.milliseconds) { edge, nanoSeconds ->
                        println("notification: $edge ${(nanoSeconds / 1_000_000) % 10_000}")
                        ++counter < 6
                    }
                    println("notTimeout: $notTimeout")
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
    println("chipTest")
    errors(label)
    active(label)
    bias(label)
    listen(label)
}
