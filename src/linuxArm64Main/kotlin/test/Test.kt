package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.gpio.GPIO_RASPBERRY_PI_5
import ch.softappeal.kopi.test.gpio.gpioTest
import ch.softappeal.kopi.test.i2c.lcd1602Test
import ch.softappeal.kopi.test.i2c.paj7620U2Test
import kotlinx.coroutines.runBlocking
import platform.posix.getpid

public fun main() {
    println("hello from process ${getpid()}")
    val gpioLabel = GPIO_RASPBERRY_PI_5
    runBlocking {
        cleanupTest()
        gpioTest(gpioLabel)
        lcd1602Test()
        paj7620U2Test(gpioLabel)
    }
    println("done")
}
