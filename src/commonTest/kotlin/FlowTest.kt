package ch.softappeal.konapi

import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class FlowTest {
    @Test
    fun sharedFlow() = runBlocking {
        val flow = MutableSharedFlow<Int>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        launch {
            delay(50.milliseconds)
            printlnCC("collecting ...")
            var count = 0
            flow.collect { value ->
                printlnCC("collect $value")
                if (++count >= 10) cancel()
                delay(25.milliseconds)
            }
        }
        repeat(30) { value ->
            printlnCC("emit $value in ${measureTime { flow.emit(value) }}")
            delay(10.milliseconds)
        }
    }
}
