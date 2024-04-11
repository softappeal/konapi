@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi.devices.bosch

import ch.softappeal.kopi.I2cDevice
import ch.softappeal.kopi.SPI_MODE_0
import ch.softappeal.kopi.SPI_MODE_4WIRE
import ch.softappeal.kopi.SPI_MODE_MSB_FIRST
import ch.softappeal.kopi.SpiDevice

/*
    Datasheet: https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bme280-ds002.pdf
        6.3 SPI interface
            In SPI mode, only 7 bits of the register addresses are used;
            the MSB of register address is not used and replaced by a read/write bit (RW = 0 for write and RW = 1 for read).
 */
public fun boschI2cAdapter(device: SpiDevice): I2cDevice {
    device.config = SpiDevice.Config(10_000_000U, 8U, SPI_MODE_0 or SPI_MODE_4WIRE or SPI_MODE_MSB_FIRST)
    fun mapWrite(register: UByte) = register and 0x7FU
    fun mapRead(register: UByte) = register or 0x80U
    return object : I2cDevice {
        override fun write(value: UByte) {
            device.write(ubyteArrayOf(value))
        }

        override fun read(): UByte {
            val bytes = UByteArray(1)
            device.transfer(bytes)
            return bytes[0]
        }

        override fun write(register: UByte, value: UByte) {
            device.write(ubyteArrayOf(mapWrite(register), value))
        }

        override fun read(register: UByte): UByte {
            val bytes = ubyteArrayOf(mapRead(register), 0U)
            device.transfer(bytes)
            return bytes[1]
        }

        override fun write(register: UByte, values: UByteArray) {
            val bytes = UByteArray(values.size + 1)
            bytes[0] = mapWrite(register)
            values.copyInto(bytes, 1)
            device.write(bytes)
        }

        override fun read(register: UByte, length: Int): UByteArray {
            val bytes = UByteArray(length + 1)
            bytes[0] = mapRead(register)
            device.transfer(bytes)
            return bytes.copyOfRange(1, length + 1)
        }
    }
}
