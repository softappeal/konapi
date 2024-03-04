package ch.softappeal.kopi.lib.i2c

/*
    Gesture Recognition Sensor

    https://www.pixart.com/products-detail/37/PAJ7620U2
    https://www.waveshare.com/wiki/PAJ7620U2_Gesture_Sensor
    https://www.waveshare.com/wiki/File:PAJ7620U2-Gesture-Sensor-Demo-Code.7z
    https://www.waveshare.com/wiki/File:PAJ7620U2_GDS-R1.0_29032016_41002AEN.pdf
*/

private class Command(val command: Int, val value: Int)

private val InitCommands = listOf(
    // ------------------------------------- select register bank 0
    Command(0xEF, 0x00),

    // Cursor Mode Controls
    Command(0x37, 0x07),
    Command(0x38, 0x17),
    Command(0x39, 0x06),
    Command(0x8B, 0x01),

    // AE/AG Controls
    Command(0x46, 0x2D),
    Command(0x47, 0x0F),
    Command(0x4A, 0x1E),
    Command(0x4C, 0x20),
    Command(0x48, 0x3C),
    Command(0x49, 0x00),
    Command(0x51, 0x10),

    // Clock Controls
    Command(0x5E, 0x10),
    Command(0x60, 0x27),

    // GPIO Setting
    Command(0x80, 0x42),
    Command(0x81, 0x44),
    Command(0x82, 0x04),

    // Gesture Mode Controls
    Command(0x90, 0x06),
    Command(0x95, 0x0A),
    Command(0x96, 0x0C),
    Command(0x97, 0x05),
    Command(0x9A, 0x14),
    Command(0x9C, 0x3F),
    Command(0xA5, 0x19),
    Command(0xCC, 0x19),
    Command(0xCD, 0x0B),
    Command(0xCE, 0x13),
    Command(0xCF, 0x64),
    Command(0xD0, 0x21),
    Command(0x83, 0x20),
    Command(0x9F, 0xF9),

    // ------------------------------------- select register bank 1
    Command(0xEF, 0x01),

    // Lens Shading Compensation
    Command(0x25, 0x01),
    Command(0x27, 0x39),
    Command(0x28, 0x7F),
    Command(0x29, 0x08),

    // Reserved Registers List
    Command(0x3E, 0xFF),
    Command(0x5E, 0x3D),
    Command(0x77, 0x01),
    Command(0x41, 0x40),
    Command(0x43, 0x30),

    // Sleep Mode
    Command(0x72, 0x01),
    Command(0x73, 0x35),
    Command(0x65, 0x96),
    Command(0x66, 0x00),
    Command(0x67, 0x97),
    Command(0x68, 0x01),
    Command(0x69, 0xCD),
    Command(0x6A, 0x01),
    Command(0x6B, 0xB0),
    Command(0x6C, 0x04),
    Command(0x6D, 0x2C),
    Command(0x6E, 0x01),
    Command(0x74, 0x00),

    // Image Size Setting
    Command(0x01, 0x1E),
    Command(0x02, 0x0F),
    Command(0x03, 0x10),
    Command(0x04, 0x02),

    // ------------------------------------- select register bank 0
    Command(0xEF, 0x00),

    // enable interrupts for all gestures
    Command(0x41, 0xFF),
    Command(0x42, 0x01),
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

public suspend fun paj7620U2(device: I2cDevice): Paj7620U2 = Paj7620U2(device).apply { init() }

public class Paj7620U2 internal constructor(private val device: I2cDevice) {
    internal suspend fun init() {
        suspend fun checkPartId() = check(device.read(0x00U).toInt() == 32)
        try {
            checkPartId()
        } catch (ignored: Exception) {
            checkPartId() // TODO: seems to fail often on first try
        }
        InitCommands.forEach { device.write(it.command.toUByte(), it.value.toUByte()) }
    }

    public suspend fun gesture(): Gesture? {
        val gesture = (device.read(0x44U).toInt() shl 8) + device.read(0x43U).toInt()
        return Gesture.entries.firstOrNull { it.value == gesture }
    }
}
