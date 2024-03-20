package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.gpio.findGpioLabel
import ch.softappeal.kopi.test.gpio.gpioTest
import ch.softappeal.kopi.test.i2c.lcd1602Test
import kotlinx.coroutines.runBlocking
import platform.posix.getpid

public fun main() {
    println("hello from process ${getpid()}")
    val gpioLabel = findGpioLabel()
    runBlocking {
        cleanupTest()
        gpioTest(gpioLabel)
        lcd1602Test()
        // bme280Test()
        // paj7620U2Test(gpioLabel)
    }
    println("done")
}
