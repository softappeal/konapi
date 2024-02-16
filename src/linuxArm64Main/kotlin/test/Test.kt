package ch.softappeal.kopi.test

import platform.posix.getpid

public fun main() {
    println("hello from process ${getpid()}")
    cleanupTest()
    chipTest()
    println("done")
}
