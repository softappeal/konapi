@file:OptIn(ExperimentalForeignApi::class)

package ch.softappeal.konapi

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import platform.posix.O_RDONLY
import platform.posix.close
import platform.posix.fstat
import platform.posix.nanosleep
import platform.posix.open
import platform.posix.read
import platform.posix.stat
import platform.posix.timespec

public actual fun readFile(path: String): ByteArray {
    val file = open(path, O_RDONLY)
    check(file >= 0) { "file '$path' not found" }
    return tryFinally({
        val size = memScoped {
            val stat = alloc<stat>()
            check(fstat(file, stat.ptr) == 0) { "fstat failed" }
            stat.st_size
        }
        val buffer = ByteArray(size.convert())
        buffer.usePinned { pinned ->
            check(read(file, pinned.addressOf(0), size.convert()) == size) { "read failed" }
        }
        buffer
    }) {
        check(close(file) == 0) { "close('$path') failed" }
    }
}

@Suppress("SpellCheckingInspection")
public actual fun sleepMs(milliSeconds: Int) {
    val spec = cValue<timespec> {
        tv_sec = milliSeconds.toLong() / 1000
        tv_nsec = (milliSeconds.toLong() % 1000) * 1_000_000
    }
    check(nanosleep(spec, null) == 0) { "nanosleep failed" }
}
