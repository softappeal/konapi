package ch.softappeal.konapi.devices

import ch.softappeal.konapi.GPIO_DISPLAY_DC
import ch.softappeal.konapi.GPIO_DISPLAY_RST
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.devices.waveshare.bwOled1in3
import ch.softappeal.konapi.devices.waveshare.color16Oled1in5
import ch.softappeal.konapi.graphics.BLACK
import ch.softappeal.konapi.graphics.BLUE
import ch.softappeal.konapi.graphics.CYAN
import ch.softappeal.konapi.graphics.Color
import ch.softappeal.konapi.graphics.GREEN
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.MAGENTA
import ch.softappeal.konapi.graphics.RED
import ch.softappeal.konapi.graphics.WHITE
import ch.softappeal.konapi.graphics.YELLOW
import ch.softappeal.konapi.graphics.draw
import ch.softappeal.konapi.graphics.fillRect
import ch.softappeal.konapi.graphics.imageOfMe
import ch.softappeal.konapi.sleepMs
import ch.softappeal.konapi.spiDeviceBus0CS0
import ch.softappeal.konapi.spiDeviceBus0CS1
import ch.softappeal.konapi.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.time.measureTime

private fun Graphics.test() {
    set(BLACK)
    assertSame(BLACK, color)
    fillRect()
    var x = 0
    val colors = listOf(BLUE, GREEN, RED, BLACK, WHITE, CYAN, MAGENTA, YELLOW)
    val stripes = 8
    val w = width / colors.size / stripes
    colors.forEach { c ->
        repeat(stripes) { s ->
            fun map(color: Int) = color / (s + 1)
            color = Color(map(c.red), map(c.green), map(c.blue))
            fillRect(x, 0, w, height)
            x += w
        }
        println("${measureTime { update() }}")
        sleepMs(1000)
    }
    set(BLACK).fillRect()
    set(RED).setPixel(0, 0)
    set(YELLOW).setPixel(width - 1, 0)
    set(GREEN).setPixel(0, height - 1)
    set(WHITE).setPixel(width - 1, height - 1)
    set(BLUE).setPixel(20, 10)
    update()
    sleepMs(1000)
}

abstract class OledTest {
    @Test
    fun color16Oled1in5() {
        spiDeviceBus0CS0().use { device ->
            Gpio().use { gpio ->
                color16Oled1in5(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device).use { display ->
                    with(display.graphics) {
                        test()
                        val newColor = Color(1, 2, 3)
                        color = newColor
                        draw(0, 0, imageOfMe)
                        assertEquals(newColor, color)
                        update()
                    }
                    sleepMs(5000)
                }
            }
        }
    }

    @Test
    fun bwOled1in3() {
        spiDeviceBus0CS1().use { device ->
            Gpio().use { gpio ->
                bwOled1in3(gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST, device).use { display -> display.graphics.test() }
            }
        }
    }
}
