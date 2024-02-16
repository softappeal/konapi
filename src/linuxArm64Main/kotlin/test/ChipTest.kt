package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.Active
import ch.softappeal.kopi.lib.Bias
import ch.softappeal.kopi.lib.Chip
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

private const val OPEN_IN = 27 // not connected
private const val IN = 22 // NOTE: connected to OUT
private const val OUT = 17

private fun errors() {
    class MyChip : Chip() {
        val out = output(OUT, false)
        val `in` = input(IN, Bias.Disable)
    }

    val myChip = MyChip()
    assertFalse(myChip.`in`.get())
    myChip.close()
    println(assertFails {
        myChip.out.set(false)
    })
    println(assertFails {
        myChip.`in`.get()
    })

    val chip1 = Chip()
    val chip2 = Chip() // opening the same chip twice seems to be legal
    chip2.close()
    chip1.close()

    Chip().use { chip ->
        assertTrue(chip.input(OPEN_IN, Bias.PullUp).get())
        println(assertFails {
            chip.input(OPEN_IN, Bias.PullUp)
        })
        println(assertFails {
            chip.listen(OPEN_IN, Bias.PullUp) { _, _ -> true }
        })
        println(assertFails {
            chip.output(OPEN_IN, false)
        })
    }
}

private fun active() {
    Chip().use { chip ->
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
    Chip().use { chip ->
        val out = chip.output(OUT, false, Active.Low)
        val `in` = chip.input(IN, Bias.Disable)
        assertTrue(`in`.get())
        out.set(true)
        assertFalse(`in`.get())
    }
    Chip().use { chip ->
        val out = chip.output(OUT, false)
        val `in` = chip.input(IN, Bias.Disable, Active.Low)
        assertTrue(`in`.get())
        out.set(true)
        assertFalse(`in`.get())
    }
    Chip().use { chip ->
        val out = chip.output(OUT, false, Active.Low)
        val `in` = chip.input(IN, Bias.Disable, Active.Low)
        assertFalse(`in`.get())
        out.set(true)
        assertTrue(`in`.get())
    }
}

private fun bias() {
    Chip().use { assertTrue(it.input(OPEN_IN, Bias.PullUp).get()) }
    Chip().use { assertFalse(it.input(OPEN_IN, Bias.PullDown).get()) }
    Chip().use { assertFalse(it.input(OPEN_IN, Bias.PullUp, Active.Low).get()) }
    Chip().use { assertTrue(it.input(OPEN_IN, Bias.PullDown, Active.Low).get()) }
}

private fun listen() {
    Chip().use { chip ->
        val out = chip.output(OUT, false)
        chip.input(IN, Bias.PullUp).use { assertFalse(it.get()) }
        runBlocking {
            launch(Dispatchers.Default) {
                repeat(2) {
                    var counter = 0
                    println("listen start")
                    chip.listen(IN, Bias.Disable) { edge, nanoSeconds ->
                        println("notification: $edge ${(nanoSeconds / 1_000_000) % 10_000}")
                        ++counter < 5
                    }
                    println("listen end")
                }
            }
            repeat(5) {
                delay(100.milliseconds)
                println(true)
                out.set(true)
                delay(100.milliseconds)
                println(false)
                out.set(false)
            }
        }
    }
}

public fun chipTest() {
    println("chipTest")
    errors()
    active()
    bias()
    listen()
}
