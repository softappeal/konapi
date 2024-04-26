package sample

import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.use

fun main() {
    Gpio().use { gpio ->
        gpio.input(25, Gpio.Bias.Disable).use { input ->
            println("input: ${input.get()}")
        }
    }
}
