@file:OptIn(ExperimentalForeignApi::class)

package ch.softappeal.konapi

import ch.softappeal.konapi.native.gpio.GPIOD_LINE_BIAS_DISABLED
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_BIAS_PULL_DOWN
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_BIAS_PULL_UP
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_DIRECTION_INPUT
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_DIRECTION_OUTPUT
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_VALUE_ACTIVE
import ch.softappeal.konapi.native.gpio.GPIOD_LINE_VALUE_INACTIVE
import ch.softappeal.konapi.native.gpio.gpiod_chip_close
import ch.softappeal.konapi.native.gpio.gpiod_chip_open
import ch.softappeal.konapi.native.gpio.gpiod_chip_request_lines
import ch.softappeal.konapi.native.gpio.gpiod_line_config_add_line_settings
import ch.softappeal.konapi.native.gpio.gpiod_line_config_free
import ch.softappeal.konapi.native.gpio.gpiod_line_config_new
import ch.softappeal.konapi.native.gpio.gpiod_line_direction
import ch.softappeal.konapi.native.gpio.gpiod_line_request_get_value
import ch.softappeal.konapi.native.gpio.gpiod_line_request_release
import ch.softappeal.konapi.native.gpio.gpiod_line_request_set_value
import ch.softappeal.konapi.native.gpio.gpiod_line_settings_free
import ch.softappeal.konapi.native.gpio.gpiod_line_settings_new
import ch.softappeal.konapi.native.gpio.gpiod_line_settings_set_active_low
import ch.softappeal.konapi.native.gpio.gpiod_line_settings_set_bias
import ch.softappeal.konapi.native.gpio.gpiod_line_settings_set_direction
import ch.softappeal.konapi.native.gpio.gpiod_line_settings_set_output_value
import ch.softappeal.konapi.native.gpio.gpiod_line_value
import cnames.structs.gpiod_line_request
import cnames.structs.gpiod_line_settings
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned

/*
    gpiodetect -v
        gpiodetect (libgpiod) v2.2.1
    curl -o src/nativeInterop/cinterop/headers/gpiod.h 'https://git.kernel.org/pub/scm/libs/libgpiod/libgpiod.git/plain/include/gpiod.h?h=v2.2.1'

    ldd /usr/bin/gpiodetect
        libgpiod.so.3 => /lib/aarch64-linux-gnu/libgpiod.so.3
    scp me@pi0:/lib/aarch64-linux-gnu/libgpiod.so.3 src/nativeInterop/cinterop/libs/libgpiod.so

    https://libgpiod.readthedocs.io/en/stable/index.html
    https://github.com/brgl/libgpiod/tree/v2.2.x/examples
 */

// The issue where the code compiles but IntelliJ shows a red error is a common symptom in Kotlin Multiplatform projects when using cinterop with opaque structs.

private fun lineValue(value: Boolean) = if (value) GPIOD_LINE_VALUE_ACTIVE else GPIOD_LINE_VALUE_INACTIVE

private fun lineValue(value: gpiod_line_value) = when (value) {
    GPIOD_LINE_VALUE_INACTIVE -> false
    GPIOD_LINE_VALUE_ACTIVE -> true
    else -> error("illegal gpiod_line_value $value")
}

private fun lineBias(bias: Gpio.Bias) = when (bias) {
    Gpio.Bias.Disable -> GPIOD_LINE_BIAS_DISABLED
    Gpio.Bias.PullDown -> GPIOD_LINE_BIAS_PULL_DOWN
    Gpio.Bias.PullUp -> GPIOD_LINE_BIAS_PULL_UP
}

public actual fun Gpio(path: String): Gpio {
    val chip = gpiod_chip_open(path) ?: error("no chip with path '$path'")

    fun requestLine(
        line: Int, direction: gpiod_line_direction, active: Gpio.Active, config: (settings: CPointer<gpiod_line_settings>) -> Unit,
    ): CPointer<gpiod_line_request> {
        val settings = gpiod_line_settings_new()!!
        check(0 == gpiod_line_settings_set_direction(settings, direction)) { "gpiod_line_settings_set_direction" }
        gpiod_line_settings_set_active_low(settings, active == Gpio.Active.Low)
        config(settings)

        val lineConfig = gpiod_line_config_new()
        uintArrayOf(line.convert()).usePinned { pinned ->
            check(0 == gpiod_line_config_add_line_settings(lineConfig, pinned.addressOf(0), 1.convert(), settings)) {
                "gpiod_line_config_add_line_settings"
            }
        }
        gpiod_line_settings_free(settings)

        val request = gpiod_chip_request_lines(chip, null, lineConfig)!!
        gpiod_line_config_free(lineConfig)
        return request
    }

    return object : Gpio {
        override fun output(line: Int, initValue: Boolean, active: Gpio.Active): Gpio.Output {
            val request = requestLine(line, GPIOD_LINE_DIRECTION_OUTPUT, active) { settings ->
                check(0 == gpiod_line_settings_set_output_value(settings, lineValue(initValue))) { "requestLine" }
            }
            return object : Gpio.Output {
                override fun set(value: Boolean) =
                    check(0 == gpiod_line_request_set_value(request, line.convert(), lineValue(value))) { "line_request_set_value" }

                override fun close() = gpiod_line_request_release(request)
            }
        }

        override fun input(line: Int, bias: Gpio.Bias, active: Gpio.Active): Gpio.Input {
            val request = requestLine(line, GPIOD_LINE_DIRECTION_INPUT, active) { settings ->
                check(0 == gpiod_line_settings_set_bias(settings, lineBias(bias))) { "gpiod_line_settings_set_bias" }
            }
            return object : Gpio.Input {
                override fun get() = lineValue(gpiod_line_request_get_value(request, line.convert()))
                override fun close() = gpiod_line_request_release(request)
            }
        }

        override fun close() {
            gpiod_chip_close(chip)
        }
    }
}
