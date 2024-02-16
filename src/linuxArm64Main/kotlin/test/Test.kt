package ch.softappeal.kopi.test

import kotlinx.coroutines.runBlocking
import platform.posix.getpid

public fun main() {
    println("hello from process ${getpid()}")
    runBlocking {
        cleanupTest()
        chipTest()
    }
}
