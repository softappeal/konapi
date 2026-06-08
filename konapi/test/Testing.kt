package ch.softappeal.konapi

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

inline fun <reified E : Exception> assertFailsMessage(expectedMessage: String, block: () -> Unit) =
    assertEquals(expectedMessage, assertFailsWith(E::class, block).message)
