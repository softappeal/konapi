package ch.softappeal.konapi.devices.waveshare

import ch.softappeal.konapi.I2cDevice

/*
    Gesture Recognition Sensor

    https://www.pixart.com/products-detail/37/PAJ7620U2
    https://www.waveshare.com/wiki/PAJ7620U2_Gesture_Sensor
    https://www.waveshare.com/wiki/File:PAJ7620U2-Gesture-Sensor-Demo-Code.7z
    https://www.waveshare.com/wiki/File:PAJ7620U2_GDS-R1.0_29032016_41002AEN.pdf
 */

public class Paj7620U2(private val device: I2cDevice) {
    init {
        fun checkPartId() = check(device.read(0x00U).toInt() == 32) { "device isn't a Paj7620U2" }
        try {
            checkPartId()
        } catch (ignored: Exception) {
            checkPartId() // seems to fail often on first try
        }
        with(device) {
            // ------------------------------------- select register bank 0
            write(0xEFU, 0x00U)

            // Cursor Mode Controls
            write(0x37U, 0x07U)
            write(0x38U, 0x17U)
            write(0x39U, 0x06U)
            write(0x8BU, 0x01U)

            // AE/AG Controls
            write(0x46U, 0x2DU)
            write(0x47U, 0x0FU)
            write(0x4AU, 0x1EU)
            write(0x4CU, 0x20U)
            write(0x48U, 0x3CU)
            write(0x49U, 0x00U)
            write(0x51U, 0x10U)

            // Clock Controls
            write(0x5EU, 0x10U)
            write(0x60U, 0x27U)

            // GPIO Setting
            write(0x80U, 0x42U)
            write(0x81U, 0x44U)
            write(0x82U, 0x04U)

            // Gesture Mode Controls
            write(0x90U, 0x06U)
            write(0x95U, 0x0AU)
            write(0x96U, 0x0CU)
            write(0x97U, 0x05U)
            write(0x9AU, 0x14U)
            write(0x9CU, 0x3FU)
            write(0xA5U, 0x19U)
            write(0xCCU, 0x19U)
            write(0xCDU, 0x0BU)
            write(0xCEU, 0x13U)
            write(0xCFU, 0x64U)
            write(0xD0U, 0x21U)
            write(0x83U, 0x20U)
            write(0x9FU, 0xF9U)

            // ------------------------------------- select register bank 1
            write(0xEFU, 0x01U)

            // Lens Shading Compensation
            write(0x25U, 0x01U)
            write(0x27U, 0x39U)
            write(0x28U, 0x7FU)
            write(0x29U, 0x08U)

            // Reserved Registers List
            write(0x3EU, 0xFFU)
            write(0x5EU, 0x3DU)
            write(0x77U, 0x01U)
            write(0x41U, 0x40U)
            write(0x43U, 0x30U)

            // Sleep Mode
            write(0x72U, 0x01U)
            write(0x73U, 0x35U)
            write(0x65U, 0x96U)
            write(0x66U, 0x00U)
            write(0x67U, 0x97U)
            write(0x68U, 0x01U)
            write(0x69U, 0xCDU)
            write(0x6AU, 0x01U)
            write(0x6BU, 0xB0U)
            write(0x6CU, 0x04U)
            write(0x6DU, 0x2CU)
            write(0x6EU, 0x01U)
            write(0x74U, 0x00U)

            // Image Size Setting
            write(0x01U, 0x1EU)
            write(0x02U, 0x0FU)
            write(0x03U, 0x10U)
            write(0x04U, 0x02U)

            // ------------------------------------- select register bank 0
            write(0xEFU, 0x00U)

            // enable interrupts for all gestures
            write(0x41U, 0xFFU)
            write(0x42U, 0x01U)
        }
        gesture() // seems to be necessary so that interrupts work
    }

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

    public fun gesture(): Gesture? {
        val gesture = (device.read(0x44U).toInt() shl 8) + device.read(0x43U).toInt()
        return Paj7620U2.Gesture.entries.firstOrNull { it.value == gesture }
    }
}
