package ch.softappeal.kopi.devices

import ch.softappeal.kopi.I2C_ADDRESS_BME280
import ch.softappeal.kopi.I2C_BUS
import ch.softappeal.kopi.I2cBus
import ch.softappeal.kopi.devices.bosch.Bme280
import ch.softappeal.kopi.devices.bosch.BoschDevice
import ch.softappeal.kopi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

abstract class Bme280Test {
    @Test
    fun test() = runBlocking {
        I2cBus(I2C_BUS).use { bus ->
            val bme280 = Bme280(BoschDevice(bus.device(I2C_ADDRESS_BME280)))
            repeat(3) {
                delay(1500.milliseconds)
                println(bme280.measurements())
            }
        }
    }
}
