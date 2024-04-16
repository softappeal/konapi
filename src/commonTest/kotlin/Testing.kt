package ch.softappeal.konapi

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

inline fun <reified T : Throwable> assertFailsMessage(expectedMessage: String, block: () -> Unit) =
    assertEquals(expectedMessage, assertFailsWith(T::class, block).message)
