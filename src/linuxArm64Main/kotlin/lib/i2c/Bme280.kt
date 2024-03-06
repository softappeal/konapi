package ch.softappeal.kopi.lib.i2c

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/*
    Combined digital humidity, pressure and temperature sensor

    https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bme280-ds002.pdf
    https://github.com/boschsensortec/BME280_SensorAPI
*/

private data class Compensation(
    val t1: Int,
    val t2: Int,
    val t3: Int,
    val p1: Int,
    val p2: Int,
    val p3: Int,
    val p4: Int,
    val p5: Int,
    val p6: Int,
    val p7: Int,
    val p8: Int,
    val p9: Int,
    val h1: Int,
    val h2: Int,
    val h3: Int,
    val h4: Int,
    val h5: Int,
    val h6: Int,
)

private data class Uncompensated(
    val t: Int,
    val p: Int,
    val h: Int,
)

public data class Measurements(
    val temperaturInCelsius: Double,
    val pressureInPascal: Double,
    val humidityInPercent: Double,
)

private fun Compensation.measurements(uncompensated: Uncompensated): Measurements {
    var var1 = ((uncompensated.t.toDouble()) / 16384.0 - (t1.toDouble()) / 1024.0) * (t2.toDouble())
    var var2 = (((uncompensated.t.toDouble()) / 131072.0 - (t1.toDouble()) / 8192.0) *
        ((uncompensated.t.toDouble()) / 131072.0 - (t1.toDouble()) / 8192.0)) * (t3.toDouble())
    val tFine = (var1 + var2).toInt().toDouble()
    val temperature = (var1 + var2) / 5120.0
    var1 = (tFine / 2.0) - 64000.0
    var2 = var1 * var1 * (p6.toDouble()) / 32768.0
    var2 += var1 * (p5.toDouble()) * 2.0
    var2 = (var2 / 4.0) + ((p4.toDouble()) * 65536.0)
    var1 = ((p3.toDouble()) * var1 * var1 / 524288.0 + (p2.toDouble()) * var1) / 524288.0
    var1 = (1.0 + var1 / 32768.0) * (p1.toDouble())
    var pressure = 0.0
    if (var1 != 0.0) {
        // avoid exception caused by division by zero
        pressure = 1048576.0 - uncompensated.p.toDouble()
        pressure = (pressure - (var2 / 4096.0)) * 6250.0 / var1
        var1 = (p9.toDouble()) * pressure * pressure / 2147483648.0
        var2 = pressure * (p8.toDouble()) / 32768.0
        pressure += (var1 + var2 + (p7.toDouble())) / 16.0
    }
    var humidity = tFine - 76800.0
    humidity =
        (uncompensated.h - ((h4.toDouble()) * 64.0 + (h5.toDouble()) / 16384.0 * humidity)) * ((h2.toDouble()) / 65536.0 * (1.0 + (h6.toDouble()) / 67108864.0 * humidity * (1.0 + (h3.toDouble()) / 67108864.0 * humidity)))
    humidity *= 1.0 - (h1.toDouble()) * humidity / 524288.0
    when {
        humidity > 100.0 -> humidity = 100.0
        humidity < 0.0 -> humidity = 0.0
    }
    return Measurements(temperature, pressure, humidity)
}

public class Bme280 internal constructor(private val device: I2cDevice) {
    @Suppress("SpellCheckingInspection")
    public suspend fun measurements(): Measurements {
        val u = device.read(0xF7U, 8)
        val uncompensated = Uncompensated(
            (u[3].toInt() shl 12) or (u[4].toInt() shl 4) or (u[5].toInt() shr 4),
            (u[0].toInt() shl 12) or (u[1].toInt() shl 4) or (u[2].toInt() shr 4),
            (u[6].toInt() shl 8) or u[7].toInt(),
        )

        val calib00to25 = device.read(0x88U, 26)
        val calib26to31 = device.read(0xE1U, 7)
        fun UByteArray.toInt(offset: Int) = (this[offset + 1].toInt() shl 8) + this[offset].toInt()
        val compensation = Compensation(
            calib00to25.toInt(0),
            calib00to25.toInt(2),
            calib00to25.toInt(4),
            calib00to25.toInt(6),
            calib00to25.toInt(8),
            calib00to25.toInt(10),
            calib00to25.toInt(12),
            calib00to25.toInt(14),
            calib00to25.toInt(16),
            calib00to25.toInt(18),
            calib00to25.toInt(20),
            calib00to25.toInt(22),
            calib00to25[25].toInt(),
            calib26to31.toInt(0),
            calib26to31[2].toInt(),
            (calib26to31[3].toInt() shl 4) + (calib26to31[4].toInt() and 0x0f),
            (calib26to31[4].toInt() and 0x0f) + (calib26to31[5].toInt() shl 4),
            calib26to31[6].toInt(),
        )

        println(uncompensated)
        println(compensation)

        return compensation.measurements(uncompensated)
    }
}

public suspend fun bme280(device: I2cDevice): Bme280 {
    device.write(0xE0U, 0xB6U) // reset
    delay(300.milliseconds) // The sensor needs some time to complete POR steps
    check(device.read(0xD0U) == 0x60U.toUByte()) { "Incorrect chip ID, NOT BME280" }
    val meas: UByte = 0xF4U
    val hum: UByte = 0xF2U
    val tempOverSampleMsk: UByte = 0xE0U
    val pressOverSampleMsk: UByte = 0x1CU
    val forced: UByte = 0x01U
    val tempSamp1: UByte = 0x20U
    val pressSamp1: UByte = 0x04U
    val humSamp1: UByte = 0x01U
    device.write(hum, device.read(hum) or humSamp1)
    device.write(meas,
        (device.read(meas) and tempOverSampleMsk.inv() and pressOverSampleMsk.inv()) or
            forced or
            tempSamp1 or pressSamp1
    )
    return Bme280(device)
}
