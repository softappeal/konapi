@file:Suppress("SpellCheckingInspection", "unused")

package ch.softappeal.kopi

const val GPIO_IN_UNCONNECTED = 27
const val GPIO_IN_CONNECTED_TO_OUT = 22
const val GPIO_OUT_CONNECTED_TO_IN = 17

const val GPIO_IN_CONNECTED_TO_PAJ7620U2_INT = 25

const val GPIO_IN_MISO_CONNECTED_TO_MOSI = 9
const val GPIO_OUT_MOSI_CONNECTED_TO_MISO = 10
const val GPIO_OUT_CE0_CONNECTED_TO_GROUNDED_LED = 8

const val I2C_BUS = 1
const val I2C_ADDRESS_BME280 = 0x76
const val I2C_ADDRESS_LCD1602 = 0x27
const val I2C_ADDRESS_PAJ7620U2 = 0x73

fun spiDevice() = SpiDevice(0, 0)
