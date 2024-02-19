package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.gpio.GPIO_RASPBERRY_PI_5
import ch.softappeal.kopi.test.gpio.gpioTest
import ch.softappeal.kopi.test.i2c.lcd1602Test
import kotlinx.coroutines.runBlocking
import platform.posix.getpid

public fun main() {
    println("hello from process ${getpid()}")
    runBlocking {
        cleanupTest()
        gpioTest(GPIO_RASPBERRY_PI_5)
        lcd1602Test()
    }
    println("done")
}
