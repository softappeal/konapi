package ch.softappeal.kopi.devices

import ch.softappeal.kopi.I2C_ADDRESS_BME280
import ch.softappeal.kopi.I2C_BUS
import ch.softappeal.kopi.I2cBus
import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.SpiDevice
import ch.softappeal.kopi.boschSpiAdapter
import ch.softappeal.kopi.spiDevice
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
        I2cBus(I2C_BUS).use { bus -> bme280Test(bus.device(I2C_ADDRESS_BME280)) }
    }

    @Test
    @Ignore
    fun spi() {
        spiDevice().use { device ->
            device.config = SpiDevice.Config(speedHz = 10_000_000U)
            bme280Test(boschSpiAdapter(device))
        }
    }
}
