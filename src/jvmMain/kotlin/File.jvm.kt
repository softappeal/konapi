@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

import java.io.File

public actual fun readFile(path: String): UByteArray = File(path).readBytes().toUByteArray()
