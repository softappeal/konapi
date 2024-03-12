package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.gpio.GPIO_RASPBERRY_PI_5
import ch.softappeal.kopi.lib.gpio.GPIO_RASPBERRY_PI_ZERO_2
import ch.softappeal.kopi.lib.gpio.Gpio
import ch.softappeal.kopi.test.gpio.gpioTest
import ch.softappeal.kopi.test.i2c.bme280Test
import ch.softappeal.kopi.test.i2c.lcd1602Test
import ch.softappeal.kopi.test.i2c.paj7620U2Test
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import platform.posix.getpid

private fun runServer() = embeddedServer(CIO, port = 8080) {
    routing {
        get("/") {
            call.respondText("Hello from kopi")
        }
    }
}.start()

public fun main() {
    println("hello from process ${getpid()}")
    var gpioLabel: String
    try {
        gpioLabel = GPIO_RASPBERRY_PI_5
        Gpio(gpioLabel).close()
    } catch (ignored: Exception) {
        gpioLabel = GPIO_RASPBERRY_PI_ZERO_2
        Gpio(gpioLabel).close()
    }
    runBlocking {
        cleanupTest()
        gpioTest(gpioLabel)
        bme280Test()
        lcd1602Test()
        runServer()
        paj7620U2Test(gpioLabel)
    }
    println("done")
}
