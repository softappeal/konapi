@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

public expect fun readFile(path: String): UByteArray
