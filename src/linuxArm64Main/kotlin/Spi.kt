@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("SpellCheckingInspection")

package ch.softappeal.kopi

import ch.softappeal.kopi.native.spi.spiIocMessage
import ch.softappeal.kopi.native.spi.spi_ioc_transfer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toLong
import kotlinx.cinterop.usePinned
import platform.posix.O_RDWR
import platform.posix.close
import platform.posix.ioctl
import platform.posix.open

/*
    https://www.kernel.org/doc/html/v5.5/spi/spidev.html

    precondition: enable SPI in raspi-config

    scp guru@raspberrypi:/usr/include/linux/spi/\* src/nativeInterop/cinterop/headers/linux/spi
 */

public actual fun SpiDevice(bus: Int, chipSelect: Int): SpiDevice {
    val file = open("/dev/spidev$bus.$chipSelect", O_RDWR)
    check(file >= 0) { "can't open SpiDevice on bus $bus with chipSelect $chipSelect" }
    return object : SpiDevice {
        override fun transfer(bytes: UByteArray) {
            memScoped {
                val transfer = alloc<spi_ioc_transfer>()
                bytes.usePinned { pinned ->
                    transfer.apply {
                        tx_buf = pinned.addressOf(0).toLong().toULong()
                        rx_buf = tx_buf
                        len = bytes.size.toUInt()
                        speed_hz = 10_000_000.toUInt()
                    }
                    check(ioctl(file, spiIocMessage(1), transfer) != -1) { "ioctl failed" }
                }
            }
        }

        override fun close() {
            check(close(file) == 0) { "close failed" }
        }
    }
}
