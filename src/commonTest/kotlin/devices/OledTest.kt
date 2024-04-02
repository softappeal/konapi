package ch.softappeal.kopi.devices

import ch.softappeal.kopi.GPIO_DISPLAY_DC
import ch.softappeal.kopi.GPIO_DISPLAY_RST
import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.devices.waveshare.Oled1in3Monochrome
import ch.softappeal.kopi.devices.waveshare.Oled1in5Color
import ch.softappeal.kopi.graphics.BLACK
import ch.softappeal.kopi.graphics.BLUE
import ch.softappeal.kopi.graphics.CYAN
import ch.softappeal.kopi.graphics.GREEN
import ch.softappeal.kopi.graphics.Graphics
import ch.softappeal.kopi.graphics.MAGENTA
import ch.softappeal.kopi.graphics.RED
import ch.softappeal.kopi.graphics.WHITE
import ch.softappeal.kopi.graphics.YELLOW
import ch.softappeal.kopi.graphics.clear
import ch.softappeal.kopi.graphics.fillRect
import ch.softappeal.kopi.spiDeviceBus0CS0
import ch.softappeal.kopi.spiDeviceBus0CS1
import ch.softappeal.kopi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

private suspend fun Graphics.test() {
    var x = 0
    val colors = listOf(BLUE, GREEN, RED, BLACK, WHITE, CYAN, MAGENTA, YELLOW)
    val w = width / colors.size
    colors.forEach { color ->
        fillRect(x, 0, w, height, color)
        x += w
        println("${measureTime { update() }}")
        delay(1.seconds)
    }
    clear()
    setPixel(0, 0, RED)
    setPixel(width - 1, 0, YELLOW)
    setPixel(0, height - 1, GREEN)
    setPixel(width - 1, height - 1, WHITE)
    update()
    delay(1.seconds)
}

abstract class OledTest {
    @Test
    fun oled1in5Color() = runBlocking {
        spiDeviceBus0CS0().use { device ->
            Gpio().use { gpio ->
                Oled1in5Color(device, gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST).use { display -> display.graphics.test() }
            }
        }
    }

    @Test
    fun oled1in3Monochrome() = runBlocking {
        spiDeviceBus0CS1().use { device ->
            Gpio().use { gpio ->
                Oled1in3Monochrome(device, gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST).use { display -> display.graphics.test() }
            }
        }
    }
}
