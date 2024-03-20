package ch.softappeal.kopi.test

import ch.softappeal.kopi.lib.Closeable
import ch.softappeal.kopi.lib.SuspendCloseable
import ch.softappeal.kopi.lib.tryFinally
import ch.softappeal.kopi.lib.use
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame
import kotlin.test.assertTrue

private fun noTryExceptionNoFinallyException() {
    var tryCalled = false
    var finallyCalled = false
    assertEquals(
        123,
        tryFinally({
            tryCalled = true
            123
        }) {
            finallyCalled = true
        }
    )
    assertTrue(tryCalled)
    assertTrue(finallyCalled)
}

private fun withTryExceptionNoFinallyException() {
    var tryCalled = false
    val tryException = Exception()
    var finallyCalled = false
    assertSame(
        tryException,
        assertFails {
            tryFinally({
                tryCalled = true
                throw tryException
            }) {
                finallyCalled = true
            }
        }
    )
    assertTrue(tryCalled)
    assertTrue(finallyCalled)
}

private fun noTryExceptionWithFinallyException() {
    var tryCalled = false
    var finallyCalled = false
    val finallyException = Exception()
    assertSame(
        finallyException,
        assertFails {
            tryFinally({
                tryCalled = true
            }) {
                finallyCalled = true
                throw finallyException
            }
        }
    )
    assertTrue(tryCalled)
    assertTrue(finallyCalled)
}

private fun withTryExceptionWithFinallyException() {
    var tryCalled = false
    val tryException = Exception()
    var finallyCalled = false
    val finallyException = Exception()
    assertSame(
        tryException,
        assertFails {
            tryFinally({
                tryCalled = true
                throw tryException
            }) {
                finallyCalled = true
                throw finallyException
            }
        }
    )
    assertTrue(tryCalled)
    assertTrue(finallyCalled)
    assertEquals(listOf(finallyException), tryException.suppressedExceptions)
}

private suspend fun withSuspend() {
    var tryCalled = false
    var finallyCalled = false

    @Suppress("RedundantSuspendModifier")
    suspend fun tryBlock(): Int {
        tryCalled = true
        return 123
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun finallyBlock(): Int {
        finallyCalled = true
        return 321
    }

    assertEquals(
        123,
        tryFinally({
            tryBlock()
        }) {
            finallyBlock()
        }
    )
    assertTrue(tryCalled)
    assertTrue(finallyCalled)
}

private fun closeable() {
    val closeable = object : Closeable {
        var used = false
        var closed = false
        override fun close() {
            closed = true
        }
    }
    assertTrue(closeable.use {
        it.used = true
        true
    })
    assertTrue(closeable.used)
    assertTrue(closeable.closed)
}

private suspend fun suspendCloseable() {
    val closeable = object : SuspendCloseable {
        var used = false
        var closed = false
        override suspend fun close() {
            closed = true
        }
    }
    assertTrue(closeable.use {
        it.used = true
        true
    })
    assertTrue(closeable.used)
    assertTrue(closeable.closed)
}

public suspend fun cleanupTest() {
    println("cleanupTest")
    noTryExceptionNoFinallyException()
    withTryExceptionNoFinallyException()
    noTryExceptionWithFinallyException()
    withTryExceptionWithFinallyException()
    withSuspend()
    closeable()
    suspendCloseable()
}
