package ch.softappeal.konapi

import java.io.File

public actual fun readFile(path: String): ByteArray = File(path).readBytes()

public actual fun sleepMs(milliSeconds: Int) {
    Thread.sleep(milliSeconds.toLong())
}
