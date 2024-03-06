package ch.softappeal.kopi.test.i2c

import ch.softappeal.kopi.lib.i2c.I2c
import ch.softappeal.kopi.lib.i2c.bme280
import ch.softappeal.kopi.lib.use
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

public suspend fun bme280Test() {
    I2c(1).use { i2c ->
        val bme280 = bme280(i2c.device(0x076))
        repeat(5) {
            delay(2.seconds)
            println(bme280.measurements())
            println()
        }
    }
}
