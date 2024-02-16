package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.Active
import ch.softappeal.kopi.lib.Bias
import ch.softappeal.kopi.lib.Chip
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.delay
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val BUTTON = 27
private const val LED = 17

private class MyChip : Chip() {
    val led = output(LED, false)
    val button = input(BUTTON, Bias.PullUp)
}

public suspend fun chipTest() {
    val myChip = MyChip()
    println("myChip.button: ${myChip.button()}")
    myChip.close()
    println(assertFails {
        myChip.led(false)
    })
    println(assertFails {
        myChip.button()
    })

    val chip1 = Chip()
    val chip2 = Chip() // opening the same chip twice seems to be legal
    chip2.close()
    chip1.close()

    Chip().use {
        input(BUTTON, Bias.PullUp)
        println(assertFails {
            input(BUTTON, Bias.PullUp)
        })
        println(assertFails {
            listen(BUTTON, Bias.PullUp) { _, _ -> true }
        })
        println(assertFails {
            output(BUTTON, false)
        })
    }

    Chip().use { output(LED, true) }
    delay(1.seconds)
    Chip().use { output(LED, false) }
    delay(1.seconds)
    Chip().use { output(LED, false, Active.Low) }
    delay(1.seconds)
    Chip().use { output(LED, true, Active.Low) }
    delay(1.seconds)

    Chip().use { println("Disable  High: ${input(BUTTON, Bias.Disable)()}") }
    Chip().use { println("PullUp   High: ${input(BUTTON, Bias.PullUp)()}") }
    Chip().use { println("PullDown High: ${input(BUTTON, Bias.PullDown)()}") }
    Chip().use { println("Disable  Low : ${input(BUTTON, Bias.Disable, Active.Low)()}") }
    Chip().use { println("PullUp   Low : ${input(BUTTON, Bias.PullUp, Active.Low)()}") }
    Chip().use { println("PullDown Low : ${input(BUTTON, Bias.PullDown, Active.Low)()}") }

    Chip().use {
        val led = output(LED, false)
        val button = input(BUTTON, Bias.PullUp)
        led(true)
        while (button()) delay(100.milliseconds)
        led(false)
    }

    Chip().use {
        repeat(2) {
            var counter = 0
            listen(BUTTON, Bias.PullUp) { edge, nanoSeconds ->
                println("notification: $edge $nanoSeconds")
                ++counter < 5
            }
        }
    }
}
