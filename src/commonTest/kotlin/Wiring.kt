@file:Suppress("SpellCheckingInspection", "unused")

package ch.softappeal.kopi

const val GPIO_IN_UNCONNECTED = 27
const val GPIO_IN_CONNECTED_TO_OUT = 22
const val GPIO_OUT_CONNECTED_TO_IN = 17

const val GPIO_PAJ7620U2_INT = 25

const val GPIO_DISPLAY_DC = 5
const val GPIO_DISPLAY_RST = 6

fun spiDeviceBus0CS0() = SpiDevice(0, 0)
fun spiDeviceBus0CS1() = SpiDevice(0, 1)
const val GPIO_SPI0_MISO = 9
const val GPIO_SPI0_MOSI = 10
const val GPIO_SPI0_CE0 = 8
const val GPIO_SPI0_CE1 = 7

fun i2cBus1() = I2cBus(1)
const val I2C_ADDRESS_BME280 = 0x76
const val I2C_ADDRESS_LCD1602 = 0x27
const val I2C_ADDRESS_PAJ7620U2 = 0x73

