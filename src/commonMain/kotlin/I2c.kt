@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

public interface I2cBus : Closeable {
    public fun device(address: Int): I2cDevice
}

public expect fun I2cBus(bus: Int): I2cBus

public interface I2cDevice {
    // S Addr Wr [A] value [A] P
    public suspend fun write(value: UByte)

    // S Addr Rd [A] [Data] NA P
    public suspend fun read(): UByte

    // S Addr Wr [A] Comm [A] Data [A] P
    public suspend fun write(register: UByte, value: UByte)

    // S Addr Wr [A] Comm [A] S Addr Rd [A] [Data] NA P
    public suspend fun read(register: UByte): UByte

    // S Addr Wr [A] Comm [A] Data [A] Data [A] ... [A] Data [A] P
    public suspend fun write(register: UByte, values: UByteArray)

    // S Addr Wr [A] Comm [A] S Addr Rd [A] [Data] A [Data] A ... A [Data] NA P
    public suspend fun read(register: UByte, length: Int): UByteArray
}

public class I2cCommand(public val register: UByte, public val value: UByte)

public suspend fun I2cDevice.write(command: I2cCommand) {
    write(command.register, command.value)
}

public suspend fun I2cDevice.write(commands: List<I2cCommand>) {
    val bytes = UByteArray(commands.size * 2 - 1)
    bytes[0] = commands[0].value
    var i = 1
    for (c in 1..<commands.size) {
        val command = commands[c]
        bytes[i++] = command.register
        bytes[i++] = command.value
    }
    write(commands[0].register, bytes)
}
