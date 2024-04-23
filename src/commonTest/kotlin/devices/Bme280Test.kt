package ch.softappeal.konapi.devices

import ch.softappeal.konapi.I2C_ADDRESS_BME280
import ch.softappeal.konapi.I2cDevice
import ch.softappeal.konapi.devices.bosch.Bme280
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

private fun bme280Test(device: I2cDevice) = runBlocking {
    val bme280 = Bme280(device)
    repeat(3) {
        delay(1500.milliseconds)
        println(bme280.measurements())
    }
}

abstract class Bme280Test {
    @Test
    fun test() {
        i2cBus1().use { bus -> bme280Test(bus.device(I2C_ADDRESS_BME280)) }
    }
}
