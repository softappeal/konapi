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
