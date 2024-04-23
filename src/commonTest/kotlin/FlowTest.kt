package ch.softappeal.konapi

import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class FlowTest {
    @Test
    @Ignore
    fun sharedFlow() = runBlocking {
        val flow = MutableSharedFlow<Int>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        launch {
            delay(50.milliseconds)
            println("collecting ...")
            var count = 0
            flow.collect { value ->
                println("collect $value")
                if (++count >= 10) cancel()
                delay(25.milliseconds)
            }
        }
        repeat(30) { value ->
            println("emit $value in ${measureTime { assertTrue(flow.tryEmit(value)) }}")
            delay(10.milliseconds)
        }
    }
}
