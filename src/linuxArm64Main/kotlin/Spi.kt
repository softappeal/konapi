@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("SpellCheckingInspection")

package ch.softappeal.kopi

import ch.softappeal.kopi.native.spi.SPI_IOC_MESSAGE_1
import ch.softappeal.kopi.native.spi.SPI_IOC_RD_BITS_PER_WORD
import ch.softappeal.kopi.native.spi.SPI_IOC_RD_MAX_SPEED_HZ
import ch.softappeal.kopi.native.spi.SPI_IOC_RD_MODE
import ch.softappeal.kopi.native.spi.SPI_IOC_WR_BITS_PER_WORD
import ch.softappeal.kopi.native.spi.SPI_IOC_WR_MAX_SPEED_HZ
import ch.softappeal.kopi.native.spi.SPI_IOC_WR_MODE
import ch.softappeal.kopi.native.spi.spi_ioc_transfer
import kotlinx.cinterop.CPrimitiveVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.O_RDWR
import platform.posix.close
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fscanf
import platform.posix.ioctl
import platform.posix.open
import kotlin.math.min

/*
    https://www.kernel.org/doc/html/v5.5/spi/spidev.html

    precondition: enable SPI in raspi-config

    scp guru@raspberrypi:/usr/include/linux/spi/\* src/nativeInterop/cinterop/headers/linux/spi
 */

public actual fun SpiDevice(bus: Int, chipSelect: Int): SpiDevice {
    val blockSizeFile = fopen("/sys/module/spidev/parameters/bufsiz", "r")
    val blockSize = tryFinally({
        memScoped {
            val variable = alloc<IntVar>()
            check(fscanf(blockSizeFile, "%d", variable.ptr) == 1) { "fscanf failed" }
            variable.value
        }
    }) {
        fclose(blockSizeFile)
    }

    val file = open("/dev/spidev$bus.$chipSelect", O_RDWR)
    check(file >= 0) { "can't open SpiDevice on bus $bus with chipSelect $chipSelect" }

    inline fun <reified V : CPrimitiveVar, T> readIoctl(request: ULong, get: (V) -> T): T = memScoped {
        val variable = alloc<V>()
        check(ioctl(file, request, variable.ptr) != -1) { "readIoctl($request) failed" }
        get(variable)
    }

    inline fun <reified V : CPrimitiveVar, T> T?.writeIoctl(request: ULong, set: V.(T) -> Unit) {
        if (this == null) return
        memScoped {
            val variable = alloc<V> { set(this@writeIoctl) }
            check(ioctl(file, request, variable.ptr) != -1) { "writeIoctl($request) failed" }
        }
    }

    fun transfer(bytes: UByteArray, read: Boolean) {
        memScoped {
            val transfer = alloc<spi_ioc_transfer>()
            bytes.usePinned { pinned ->
                val size = bytes.size
                var index = 0
                while (index < size) {
                    val length = min(blockSize, size - index)
                    transfer.apply {
                        tx_buf = pinned.addressOf(index).toLong().convert()
                        rx_buf = if (read) tx_buf else 0U
                        len = length.convert()
                    }
                    check(ioctl(file, SPI_IOC_MESSAGE_1, transfer) != -1) { "ioctl failed" }
                    index += length
                }
            }
        }
    }

    return object : SpiDevice {
        override val blockSize = blockSize

        override var config: SpiDevice.Config
            get() = SpiDevice.Config(
                speedHz = readIoctl<UIntVar, _>(SPI_IOC_RD_MAX_SPEED_HZ) { it.value },
                bitsPerWord = readIoctl<UByteVar, _>(SPI_IOC_RD_BITS_PER_WORD) { it.value },
                mode = readIoctl<UByteVar, _>(SPI_IOC_RD_MODE) { it.value },
            )
            set(value) {
                with(value) {
                    speedHz.writeIoctl<UIntVar, _>(SPI_IOC_WR_MAX_SPEED_HZ) { this.value = it }
                    bitsPerWord.writeIoctl<UByteVar, _>(SPI_IOC_WR_BITS_PER_WORD) { this.value = it }
                    mode.writeIoctl<UByteVar, _>(SPI_IOC_WR_MODE) { this.value = it }
                }
            }

        override fun transfer(bytes: UByteArray) {
            transfer(bytes, true)
        }

        override fun write(bytes: UByteArray) {
            transfer(bytes, false)
        }

        override fun close() {
            check(close(file) == 0) { "close failed" }
        }
    }
}
