package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.gpio.RASPBERRY_PI_5
import ch.softappeal.kopi.test.gpio.chipTest
import platform.posix.getpid

public fun main() {
    println("hello from process ${getpid()}")
    cleanupTest()
    chipTest(RASPBERRY_PI_5)
    println("done")
}
