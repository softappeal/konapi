package ch.softappeal.konapi.app

import ch.softappeal.konapi.I2cBus

const val GPIO_PAJ7620U2_INT = 25

fun i2cBus1() = I2cBus(1)
const val I2C_ADDRESS_BME280 = 0x76
const val I2C_ADDRESS_LCD1602 = 0x27
const val I2C_ADDRESS_PAJ7620U2 = 0x73
