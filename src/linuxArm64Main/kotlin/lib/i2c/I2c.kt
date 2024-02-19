@file:Suppress("SpellCheckingInspection")

package ch.softappeal.kopi.lib.i2c

import ch.softappeal.kopi.lib.Closeable
import ch.softappeal.kopi.lib.i2c.native.I2C_SLAVE
import ch.softappeal.kopi.lib.i2c.native.i2c_smbus_read_i2c_block_data
import ch.softappeal.kopi.lib.i2c.native.i2c_smbus_write_byte
import ch.softappeal.kopi.lib.i2c.native.i2c_smbus_write_i2c_block_data
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.posix.O_RDWR
import platform.posix.ioctl
import platform.posix.open

// https://www.kernel.org/doc/html/v5.5/i2c/smbus-protocol.html

/*
  precondition:enable I2C in raspi-config

  i2cdetect -V
    i2cdetect version 4.3
  curl -o src/nativeInterop/cinterop/headers/include/i2c/smbus.h https://git.kernel.org/pub/scm/utils/i2c-tools/i2c-tools.git/plain/include/i2c/smbus.h\?h=v4.3
  scp guru@raspberrypi:/usr/include/linux/i2c-dev.h src/nativeInterop/cinterop/headers/include/linux/i2c-dev.h

  ldd /usr/sbin/i2cdetect
    libi2c.so.0 => /lib/aarch64-linux-gnu/libi2c.so.0 (0x00007fff45ac0000)
  scp guru@raspberrypi:/lib/aarch64-linux-gnu/libi2c.so.0 src/nativeInterop/cinterop/libi2c.so
*/

public class I2c(bus: Int) : Closeable {
    internal val file: Int = open("/dev/i2c-$bus", O_RDWR)
    internal var address: Int = 0
    internal val mutex = Mutex() // TODO: maybe sync over writes and reads must be external?

    init {
        check(file >= 0) { "can't open I2C bus $bus" }
    }

    override fun close() {
        platform.posix.close(file)
    }

    public fun device(address: Int): I2cDevice = I2cDevice(this, address)
}

public class I2cDevice internal constructor(private val i2c: I2c, private val address: Int) {
    private suspend fun <R> selectDevice(action: () -> R): R = i2c.mutex.withLock {
        if (i2c.address != address) {
            check(ioctl(i2c.file, I2C_SLAVE.toULong(), address) == 0) { "can't communicate with I2C device $address" }
            i2c.address = address
        }
        action()
    }

    // S Addr Wr [A] value [A] P
    public suspend fun write(value: UByte) {
        selectDevice {
            check(i2c_smbus_write_byte(i2c.file, value) == 0) { "byte write with I2C device $address failed" }
        }
    }

    public suspend fun write(command: UByte, values: UByteArray) {
        selectDevice {
            values.usePinned { pinned ->
                check(i2c_smbus_write_i2c_block_data(i2c.file, command, values.size.toUByte(), pinned.addressOf(0)) == 0) {
                    "block write with I2C device $address failed"
                }
            }
        }
    }

    public suspend fun read(command: UByte, length: Int): UByteArray = selectDevice {
        memScoped {
            val buffer = allocArray<UByteVar>(length)
            check(i2c_smbus_read_i2c_block_data(i2c.file, command, length.toUByte(), buffer) == length) {
                "block read with I2C device $address failed"
            }
            UByteArray(length) { buffer[it] }
        }
    }
}
