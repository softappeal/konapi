package ch.softappeal.konapi.devices

import ch.softappeal.konapi.I2C_ADDRESS_BME280
import ch.softappeal.konapi.devices.bosch.Bme280
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.sleepMs
import ch.softappeal.konapi.use
import kotlin.test.Test

abstract class Bme280Test {
    @Test
    fun test() {
        i2cBus1().use { bus ->
            val bme280 = Bme280(bus.device(I2C_ADDRESS_BME280))
            repeat(2) {
                sleepMs(1500)
                println(bme280.measurement())
            }
        }
    }
}
