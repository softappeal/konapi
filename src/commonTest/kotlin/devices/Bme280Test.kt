package ch.softappeal.kopi.devices

import ch.softappeal.kopi.I2C_ADDRESS_BME280
import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.devices.bosch.Bme280
import ch.softappeal.kopi.devices.bosch.boschI2cAdapter
import ch.softappeal.kopi.i2cBus1
import ch.softappeal.kopi.spiDeviceBus0CS0
import ch.softappeal.kopi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
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
    // @Ignore
    fun i2c() {
        i2cBus1().use { bus -> bme280Test(bus.device(I2C_ADDRESS_BME280)) }
    }

    @Test
    @Ignore
    fun spi() {
        spiDeviceBus0CS0().use { device -> bme280Test(boschI2cAdapter(device)) }
    }
}
