package ch.softappeal.konapi

import ch.softappeal.konapi.Gpio.Active
import ch.softappeal.konapi.Gpio.Bias
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun gpio(
    outputLine: Int, initValue: Boolean,
    inputLine: Int, bias: Bias,
    outputActive: Active = Active.High, inputActive: Active = Active.High,
    block: (output: Gpio.Output, input: Gpio.Input) -> Unit,
) {
    Gpio(GPIO_PATH).use { gpio ->
        println("outputLine: $outputLine, inputLine: $inputLine, initValue: $initValue, bias: $bias, outputActive: $outputActive, inputActive: $inputActive")
        gpio.output(outputLine, initValue, outputActive).use { output ->
            gpio.input(inputLine, bias, inputActive).use { input ->
                block(output, input)
            }
        }
    }
}

abstract class GpioTest {
    @Test
    fun initValue() {
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_CONNECTED_TO_OUT, Bias.Disable) { _, input ->
            assertFalse(input.get())
        }
        gpio(GPIO_OUT_CONNECTED_TO_IN, true, GPIO_IN_CONNECTED_TO_OUT, Bias.Disable) { _, input ->
            assertTrue(input.get())
        }
    }

    @Test
    fun active() {
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.High, Active.High) { output, input ->
            assertFalse(input.get())
            output.set(true)
            assertTrue(input.get())
        }
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.Low, Active.High) { output, input ->
            assertTrue(input.get())
            output.set(true)
            assertFalse(input.get())
        }
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.High, Active.Low) { output, input ->
            assertTrue(input.get())
            output.set(true)
            assertFalse(input.get())
        }
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_CONNECTED_TO_OUT, Bias.Disable, Active.Low, Active.Low) { output, input ->
            assertFalse(input.get())
            output.set(true)
            assertTrue(input.get())
        }
    }

    @Test
    fun bias() {
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_UNCONNECTED, Bias.PullUp) { _, input ->
            sleepMs(10)
            assertTrue(input.get())
        }
        gpio(GPIO_OUT_CONNECTED_TO_IN, false, GPIO_IN_UNCONNECTED, Bias.PullDown) { _, input ->
            sleepMs(10)
            assertFalse(input.get())
        }
    }
}
