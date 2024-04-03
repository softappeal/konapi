@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

public const val SPI_MODE_0: UByte = 0x00U
public const val SPI_MODE_1: UByte = 0x01U
public const val SPI_MODE_2: UByte = 0x02U
public const val SPI_MODE_3: UByte = 0x03U
public const val SPI_MODE_LSB_FIRST: UByte = 0x08U
public const val SPI_MODE_LSB_LAST: UByte = 0x00U
public const val SPI_MODE_3WIRE: UByte = 0x10U
public const val SPI_MODE_4WIRE: UByte = 0x00U

public interface SpiDevice : Closeable {
    public data class Config(
        public val speedHz: UInt? = null,
        public val bitsPerWord: UByte? = null,
        public val mode: UByte? = null,
    )

    public val blockSize: Int
    public var config: Config
    public fun transfer(bytes: UByteArray)
}

public expect fun SpiDevice(bus: Int, chipSelect: Int): SpiDevice

public fun spiAdapter(
    device: SpiDevice,
    mapWrite: (register: UByte) -> UByte,
    mapRead: (register: UByte) -> UByte,
): I2cDevice = object : I2cDevice {
    override suspend fun write(value: UByte) {
        device.transfer(ubyteArrayOf(value))
    }

    override suspend fun read(): UByte {
        val bytes = UByteArray(1)
        device.transfer(bytes)
        return bytes[0]
    }

    override suspend fun write(register: UByte, value: UByte) {
        device.transfer(ubyteArrayOf(mapWrite(register), value))
    }

    override suspend fun read(register: UByte): UByte {
        val bytes = ubyteArrayOf(mapRead(register), 0U)
        device.transfer(bytes)
        return bytes[1]
    }

    override suspend fun write(register: UByte, values: UByteArray) {
        val bytes = UByteArray(values.size + 1)
        bytes[0] = mapWrite(register)
        values.copyInto(bytes, 1)
        device.transfer(bytes)
    }

    override suspend fun read(register: UByte, length: Int): UByteArray {
        val bytes = UByteArray(length + 1)
        bytes[0] = mapRead(register)
        device.transfer(bytes)
        return bytes.copyOfRange(1, length + 1)
    }
}

/*
    Datasheet: https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bme280-ds002.pdf
        6.3 SPI interface
            In SPI mode, only 7 bits of the register addresses are used;
            the MSB of register address is not used and replaced by a read/write bit (RW = 0 for write and RW = 1 for read).
 */
public fun boschSpiAdapter(device: SpiDevice): I2cDevice = spiAdapter(
    device,
    mapWrite = { register -> register and 0x7FU },
    mapRead = { register -> register or 0x80U },
)
