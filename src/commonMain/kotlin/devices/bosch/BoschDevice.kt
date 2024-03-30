@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.devices.bosch

import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.RegisterDevice

public interface BoschDevice : RegisterDevice {
    override suspend fun write(register: UByte, value: UByte)
    public suspend fun read(register: UByte): UByte
    override suspend fun write(register: UByte, values: UByteArray)
    public suspend fun read(register: UByte, length: Int): UByteArray
}

public fun BoschDevice(device: I2cDevice): BoschDevice = object : BoschDevice {
    override suspend fun write(register: UByte, value: UByte) {
        device.write(register, value)
    }

    override suspend fun read(register: UByte) = device.read(register)

    override suspend fun write(register: UByte, values: UByteArray) {
        device.write(register, values)
    }

    override suspend fun read(register: UByte, length: Int) = device.read(register, length)
}
