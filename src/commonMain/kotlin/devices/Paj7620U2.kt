package ch.softappeal.kopi.devices

import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.RegisterCommand
import ch.softappeal.kopi.devices.Paj7620U2.Gesture
import ch.softappeal.kopi.write

/*
    Gesture Recognition Sensor

    https://www.pixart.com/products-detail/37/PAJ7620U2
    https://www.waveshare.com/wiki/PAJ7620U2_Gesture_Sensor
    https://www.waveshare.com/wiki/File:PAJ7620U2-Gesture-Sensor-Demo-Code.7z
    https://www.waveshare.com/wiki/File:PAJ7620U2_GDS-R1.0_29032016_41002AEN.pdf
 */

private val InitCommands = listOf(
    // ------------------------------------- select register bank 0
    RegisterCommand(0xEFU, 0x00U),

    // Cursor Mode Controls
    RegisterCommand(0x37U, 0x07U),
    RegisterCommand(0x38U, 0x17U),
    RegisterCommand(0x39U, 0x06U),
    RegisterCommand(0x8BU, 0x01U),

    // AE/AG Controls
    RegisterCommand(0x46U, 0x2DU),
    RegisterCommand(0x47U, 0x0FU),
    RegisterCommand(0x4AU, 0x1EU),
    RegisterCommand(0x4CU, 0x20U),
    RegisterCommand(0x48U, 0x3CU),
    RegisterCommand(0x49U, 0x00U),
    RegisterCommand(0x51U, 0x10U),

    // Clock Controls
    RegisterCommand(0x5EU, 0x10U),
    RegisterCommand(0x60U, 0x27U),

    // GPIO Setting
    RegisterCommand(0x80U, 0x42U),
    RegisterCommand(0x81U, 0x44U),
    RegisterCommand(0x82U, 0x04U),

    // Gesture Mode Controls
    RegisterCommand(0x90U, 0x06U),
    RegisterCommand(0x95U, 0x0AU),
    RegisterCommand(0x96U, 0x0CU),
    RegisterCommand(0x97U, 0x05U),
    RegisterCommand(0x9AU, 0x14U),
    RegisterCommand(0x9CU, 0x3FU),
    RegisterCommand(0xA5U, 0x19U),
    RegisterCommand(0xCCU, 0x19U),
    RegisterCommand(0xCDU, 0x0BU),
    RegisterCommand(0xCEU, 0x13U),
    RegisterCommand(0xCFU, 0x64U),
    RegisterCommand(0xD0U, 0x21U),
    RegisterCommand(0x83U, 0x20U),
    RegisterCommand(0x9FU, 0xF9U),

    // ------------------------------------- select register bank 1
    RegisterCommand(0xEFU, 0x01U),

    // Lens Shading Compensation
    RegisterCommand(0x25U, 0x01U),
    RegisterCommand(0x27U, 0x39U),
    RegisterCommand(0x28U, 0x7FU),
    RegisterCommand(0x29U, 0x08U),

    // Reserved Registers List
    RegisterCommand(0x3EU, 0xFFU),
    RegisterCommand(0x5EU, 0x3DU),
    RegisterCommand(0x77U, 0x01U),
    RegisterCommand(0x41U, 0x40U),
    RegisterCommand(0x43U, 0x30U),

    // Sleep Mode
    RegisterCommand(0x72U, 0x01U),
    RegisterCommand(0x73U, 0x35U),
    RegisterCommand(0x65U, 0x96U),
    RegisterCommand(0x66U, 0x00U),
    RegisterCommand(0x67U, 0x97U),
    RegisterCommand(0x68U, 0x01U),
    RegisterCommand(0x69U, 0xCDU),
    RegisterCommand(0x6AU, 0x01U),
    RegisterCommand(0x6BU, 0xB0U),
    RegisterCommand(0x6CU, 0x04U),
    RegisterCommand(0x6DU, 0x2CU),
    RegisterCommand(0x6EU, 0x01U),
    RegisterCommand(0x74U, 0x00U),

    // Image Size Setting
    RegisterCommand(0x01U, 0x1EU),
    RegisterCommand(0x02U, 0x0FU),
    RegisterCommand(0x03U, 0x10U),
    RegisterCommand(0x04U, 0x02U),

    // ------------------------------------- select register bank 0
    RegisterCommand(0xEFU, 0x00U),

    // enable interrupts for all gestures
    RegisterCommand(0x41U, 0xFFU),
    RegisterCommand(0x42U, 0x01U),
)

public interface Paj7620U2 {
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

    public suspend fun gesture(): Gesture?
}

public suspend fun Paj7620U2(device: I2cDevice): Paj7620U2 {
    suspend fun checkPartId() = check(device.read(0x00U).toInt() == 32) { "device isn't a Paj7620U2" }
    try {
        checkPartId()
    } catch (ignored: Exception) {
        checkPartId() // NOTE: seems to fail often on first try
    }
    InitCommands.forEach { device.write(it) }
    return object : Paj7620U2 {
        override suspend fun gesture(): Gesture? {
            val gesture = (device.read(0x44U).toInt() shl 8) + device.read(0x43U).toInt()
            return Gesture.entries.firstOrNull { it.value == gesture }
        }
    }
}
