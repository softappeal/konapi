package ch.softappeal.kopi.ic2

import ch.softappeal.kopi.I2C_ADDRESS_BME280
import ch.softappeal.kopi.I2C_BUS
import ch.softappeal.kopi.i2c.I2c
import ch.softappeal.kopi.i2c.bme280
import ch.softappeal.kopi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

abstract class Bme280Test {
    @Test
    fun test() = runBlocking {
        I2c(I2C_BUS).use { i2c ->
            val bme280 = bme280(i2c.device(I2C_ADDRESS_BME280))
            repeat(3) {
                delay(1500.milliseconds)
                println(bme280.measurements())
            }
        }
    }
}
