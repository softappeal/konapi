package ch.softappeal.kopi

import java.io.File

public actual fun readFile(path: String): ByteArray = File(path).readBytes()
