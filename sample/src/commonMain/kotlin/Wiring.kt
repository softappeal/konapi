@file:Suppress("SpellCheckingInspection", "unused")

package sample

import ch.softappeal.konapi.I2cBus
import ch.softappeal.konapi.SpiDevice

const val GPIO_SELECT_COLOR_DISPLAY = 23
const val GPIO_PAJ7620U2_INT = 25

const val GPIO_DISPLAY_DC = 5
const val GPIO_DISPLAY_RST = 6

fun colorDisplay() = SpiDevice(0, 0)
fun bwDisplay() = SpiDevice(0, 1)
const val GPIO_SPI0_MISO = 9
const val GPIO_SPI0_MOSI = 10
const val GPIO_SPI0_SCLK = 11
const val GPIO_SPI0_CE0 = 8
const val GPIO_SPI0_CE1 = 7

fun i2cBus1() = I2cBus(1)
const val GPIO_I2C_SDA = 2
const val GPIO_I2C_CLK = 3
const val I2C_ADDRESS_BME280 = 0x76
const val I2C_ADDRESS_PAJ7620U2 = 0x73
