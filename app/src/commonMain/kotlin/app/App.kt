package ch.softappeal.kopi.app

import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.devices.bosch.Bme280
import ch.softappeal.kopi.devices.hitachi.i2cLcd1602
import ch.softappeal.kopi.devices.waveshare.Paj7620U2
import ch.softappeal.kopi.use
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import kotlin.time.measureTime

private fun runServer() = embeddedServer(CIO, port = 8080) {
    routing {
        get("/") {
            call.respondText("Hello from kopi")
        }
    }
}.start()

fun main() {
    runBlocking {
        runServer()
        i2cBus1().use { bus ->
            i2cLcd1602(bus.device(I2C_ADDRESS_LCD1602)).use { lcd ->
                lcd.clear()
                lcd.setCursorPosition(1, 0)
                lcd.displayString("REBOOTED")
                Gpio().use { gpio ->
                    val paj7620U2 = Paj7620U2(bus.device(I2C_ADDRESS_PAJ7620U2))
                    val bme280 = Bme280(bus.device(I2C_ADDRESS_BME280))
                    gpio.listen(GPIO_PAJ7620U2_INT, Gpio.Bias.PullUp, 100.days) { edge, _ ->
                        if (edge == Gpio.Edge.Falling) {
                            val launchTime = measureTime {
                                launch(Dispatchers.IO) { // should be short or we will miss interrupts
                                    val displayTime = measureTime {
                                        val gesture = paj7620U2.gesture()
                                        val measurements = bme280.measurements()
                                        val string = when (gesture) {
                                            null -> "<none>"
                                            Paj7620U2.Gesture.Up -> "Temp:" + (measurements.temperaturInCelsius * 10).roundToInt() + " 0.1C"
                                            Paj7620U2.Gesture.Down -> "Press:" + (measurements.pressureInPascal / 100).roundToInt() + " hPa"
                                            Paj7620U2.Gesture.Left -> "Humid:" + (measurements.humidityInPercent * 10).roundToInt() + " 0.1%"
                                            else -> gesture.name
                                        }
                                        lcd.setCursorPosition(1, 0)
                                        lcd.displayString(string + " ".repeat(lcd.config.columns - string.length))
                                    }
                                    println("displayTime=$displayTime")
                                }
                            }
                            println("launchTime=$launchTime")
                        }
                        true
                    }
                }
            }
        }
    }
}
