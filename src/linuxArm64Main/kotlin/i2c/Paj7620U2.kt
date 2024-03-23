package ch.softappeal.kopi.i2c

import ch.softappeal.kopi.i2c.I2cDevice.Command

/*
    Gesture Recognition Sensor

    https://www.pixart.com/products-detail/37/PAJ7620U2
    https://www.waveshare.com/wiki/PAJ7620U2_Gesture_Sensor
    https://www.waveshare.com/wiki/File:PAJ7620U2-Gesture-Sensor-Demo-Code.7z
    https://www.waveshare.com/wiki/File:PAJ7620U2_GDS-R1.0_29032016_41002AEN.pdf
 */

private val InitCommands = listOf(
    // ------------------------------------- select register bank 0
    Command(0xEFU, 0x00U),

    // Cursor Mode Controls
    Command(0x37U, 0x07U),
    Command(0x38U, 0x17U),
    Command(0x39U, 0x06U),
    Command(0x8BU, 0x01U),

    // AE/AG Controls
    Command(0x46U, 0x2DU),
    Command(0x47U, 0x0FU),
    Command(0x4AU, 0x1EU),
    Command(0x4CU, 0x20U),
    Command(0x48U, 0x3CU),
    Command(0x49U, 0x00U),
    Command(0x51U, 0x10U),

    // Clock Controls
    Command(0x5EU, 0x10U),
    Command(0x60U, 0x27U),

    // GPIO Setting
    Command(0x80U, 0x42U),
    Command(0x81U, 0x44U),
    Command(0x82U, 0x04U),

    // Gesture Mode Controls
    Command(0x90U, 0x06U),
    Command(0x95U, 0x0AU),
    Command(0x96U, 0x0CU),
    Command(0x97U, 0x05U),
    Command(0x9AU, 0x14U),
    Command(0x9CU, 0x3FU),
    Command(0xA5U, 0x19U),
    Command(0xCCU, 0x19U),
    Command(0xCDU, 0x0BU),
    Command(0xCEU, 0x13U),
    Command(0xCFU, 0x64U),
    Command(0xD0U, 0x21U),
    Command(0x83U, 0x20U),
    Command(0x9FU, 0xF9U),

    // ------------------------------------- select register bank 1
    Command(0xEFU, 0x01U),

    // Lens Shading Compensation
    Command(0x25U, 0x01U),
    Command(0x27U, 0x39U),
    Command(0x28U, 0x7FU),
    Command(0x29U, 0x08U),

    // Reserved Registers List
    Command(0x3EU, 0xFFU),
    Command(0x5EU, 0x3DU),
    Command(0x77U, 0x01U),
    Command(0x41U, 0x40U),
    Command(0x43U, 0x30U),

    // Sleep Mode
    Command(0x72U, 0x01U),
    Command(0x73U, 0x35U),
    Command(0x65U, 0x96U),
    Command(0x66U, 0x00U),
    Command(0x67U, 0x97U),
    Command(0x68U, 0x01U),
    Command(0x69U, 0xCDU),
    Command(0x6AU, 0x01U),
    Command(0x6BU, 0xB0U),
    Command(0x6CU, 0x04U),
    Command(0x6DU, 0x2CU),
    Command(0x6EU, 0x01U),
    Command(0x74U, 0x00U),

    // Image Size Setting
    Command(0x01U, 0x1EU),
    Command(0x02U, 0x0FU),
    Command(0x03U, 0x10U),
    Command(0x04U, 0x02U),

    // ------------------------------------- select register bank 0
    Command(0xEFU, 0x00U),

    // enable interrupts for all gestures
    Command(0x41U, 0xFFU),
    Command(0x42U, 0x01U),
)

public enum class Gesture(internal val value: Int) {
    Up(0x01),
    Down(0x02),
    Left(0x04),
    Right(0x08),
    Forward(0x10),
    Backward(0x20),
    Clockwise(0x40),
    AntiClockwise(0x80),
    Wave(0x100),
}

public class Paj7620U2 internal constructor(private val device: I2cDevice) {
    public suspend fun gesture(): Gesture? {
        val gesture = (device.read(0x44U).toInt() shl 8) + device.read(0x43U).toInt()
        return Gesture.entries.firstOrNull { it.value == gesture }
    }
}

public suspend fun paj7620U2(device: I2cDevice): Paj7620U2 {
    suspend fun checkPartId() = check(device.read(0x00U).toInt() == 32)
    try {
        checkPartId()
    } catch (ignored: Exception) {
        checkPartId() // NOTE: seems to fail often on first try
    }
    InitCommands.forEach { device.write(it) }
    return Paj7620U2(device)
}
