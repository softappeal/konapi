package ch.softappeal.kopi

import kotlinx.coroutines.currentCoroutineContext

const val GPIO_IN_UNCONNECTED = 27

const val GPIO_IN_CONNECTED_TO_OUT = 22
const val GPIO_OUT_CONNECTED_TO_IN = 17

const val GPIO_IN_CONNECTED_TO_PAJ7620U2_INT = 26

const val I2C_BUS = 1

const val I2C_ADDRESS_BME280 = 0x76
const val I2C_ADDRESS_LCD1602 = 0x27
const val I2C_ADDRESS_PAJ7620U2 = 0x73

suspend fun printlnCC(msg: String) {
    val context = currentCoroutineContext()
    println("$context@${context.hashCode()} - $msg")
}
