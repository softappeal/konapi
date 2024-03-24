package ch.softappeal.kopi.app

import ch.softappeal.kopi.i2c.I2cBus
import ch.softappeal.kopi.i2c.lcd1602
import ch.softappeal.kopi.use
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.days

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
        I2cBus(I2C_BUS).use { i2c ->
            lcd1602(i2c.device(I2C_ADDRESS_LCD1602)).use { lcd ->
                lcd.clear()
                lcd.setCursorPosition(1, 0)
                lcd.displayString("REBOOTED")
                delay(100.days)
                /*
                val paj7620U2 = paj7620U2(i2c.device(I2C_ADDRESS_PAJ7620U2))
                val bme280 = bme280(i2c.device(I2C_ADDRESS_BME280))
                println(paj7620U2.gesture())
                coroutineScope {
                    launch(Dispatchers.Default) {
                        Gpio().use { chip ->
                            chip.listen(GPIO_IN_CONNECTED_TO_PAJ7620U2_INT, Gpio.Bias.PullUp, 100.days) { edge, _ ->
                                if (edge == Gpio.Edge.Falling) {
                                    val gesture = paj7620U2.gesture()
                                    val measurements = bme280.measurements()
                                    val string = when (gesture) {
                                        null -> "<none>"
                                        Gesture.Up -> "Temp:" + (measurements.temperaturInCelsius * 10).roundToInt() + " 0.1C"
                                        Gesture.Down -> "Press:" + (measurements.pressureInPascal / 100).roundToInt() + " hPa"
                                        Gesture.Left -> "Humid:" + (measurements.humidityInPercent * 10).roundToInt() + " 0.1%"
                                        else -> gesture.name
                                    }
                                    lcd.setCursorPosition(1, 0)
                                    lcd.displayString(string + " ".repeat(lcd.config.columns - string.length))
                                }
                                true
                            }
                        }
                    }
                }
                */
            }
        }
    }
}
