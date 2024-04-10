@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

public const val SPI_MODE_0: UByte = 0x00U
public const val SPI_MODE_1: UByte = 0x01U
public const val SPI_MODE_2: UByte = 0x02U
public const val SPI_MODE_3: UByte = 0x03U
public const val SPI_MODE_LSB_FIRST: UByte = 0x08U
public const val SPI_MODE_MSB_FIRST: UByte = 0x00U
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
    public fun write(bytes: UByteArray)
}

public expect fun SpiDevice(bus: Int, chipSelect: Int): SpiDevice

public object DummySpiDevice : SpiDevice {
    override val blockSize: Int = 0
    override fun close(): Unit = Unit
    override fun transfer(bytes: UByteArray): Unit = Unit
    override fun write(bytes: UByteArray): Unit = Unit
    override var config: SpiDevice.Config
        get() = throw NotImplementedError()
        set(_) = Unit
}
