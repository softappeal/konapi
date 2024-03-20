package ch.softappeal.kopi.test.i2c

import ch.softappeal.kopi.app.I2C_ADDRESS_BME280
import ch.softappeal.kopi.app.I2C_BUS
import ch.softappeal.kopi.lib.i2c.I2c
import ch.softappeal.kopi.lib.i2c.bme280
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

public suspend fun bme280Test() {
    println("bme280Test")
    I2c(I2C_BUS).use { i2c ->
        val bme280 = bme280(i2c.device(I2C_ADDRESS_BME280))
        repeat(3) {
            delay(1500.milliseconds)
            println(bme280.measurements())
        }
    }
}
