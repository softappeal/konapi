package ch.softappeal.konapi.devices

import ch.softappeal.konapi.DummyGpio
import ch.softappeal.konapi.DummyI2cDevice
import ch.softappeal.konapi.DummySpiDevice
import ch.softappeal.konapi.GPIO_DISPLAY_DC
import ch.softappeal.konapi.GPIO_DISPLAY_RST
import ch.softappeal.konapi.Gpio
import ch.softappeal.konapi.I2C_ADDRESS_OLED
import ch.softappeal.konapi.assertFailsMessage
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
import ch.softappeal.konapi.graphics.fillRect
import ch.softappeal.konapi.i2cBus1
import ch.softappeal.konapi.spiDeviceBus0CS0
import ch.softappeal.konapi.spiDeviceBus0CS1
import ch.softappeal.konapi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

private suspend fun Graphics.test() {
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
        delay(1.seconds)
    }
    set(BLACK).fillRect()
    set(RED).setPixel(0, 0)
    set(YELLOW).setPixel(width - 1, 0)
    set(GREEN).setPixel(0, height - 1)
    set(WHITE).setPixel(width - 1, height - 1)
    set(BLUE).setPixel(20, 10)
    update()
    delay(1.seconds)
}

abstract class OledTest {
    @Test
    fun color16Oled1in5() = runBlocking {
        spiDeviceBus0CS0().use { device ->
            Gpio().use { gpio ->
                color16Oled1in5(device, gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST).use { display -> display.graphics.test() }
            }
        }
    }

    @Test
    @Ignore
    fun bwOled1in3Spi() = runBlocking {
        spiDeviceBus0CS1().use { device ->
            Gpio().use { gpio ->
                bwOled1in3(null, device, gpio, GPIO_DISPLAY_DC, GPIO_DISPLAY_RST).use { display -> display.graphics.test() }
            }
        }
    }

    @Test
    // @Ignore
    fun bwOled1in3I2c() = runBlocking {
        i2cBus1().use { bus ->
            Gpio().use { gpio ->
                bwOled1in3(bus.device(I2C_ADDRESS_OLED), null, gpio, null, GPIO_DISPLAY_RST).use { display ->
                    display.graphics.test()
                }
            }
        }
    }

    @Test
    fun bwOled1in3InvalidConfig() = runBlocking {
        assertFailsMessage<IllegalArgumentException>("one of i2cDevice or spiDevice must be null") {
            bwOled1in3(null, null, DummyGpio, 0, 0)
        }
        assertFailsMessage<IllegalArgumentException>("one of i2cDevice or spiDevice must be null") {
            bwOled1in3(DummyI2cDevice, DummySpiDevice, DummyGpio, 0, 0)
        }
        assertFailsMessage<IllegalArgumentException>("specify dcPin only for spiDevice") {
            bwOled1in3(DummyI2cDevice, null, DummyGpio, 0, 0)
        }
        assertFailsMessage<IllegalArgumentException>("specify dcPin only for spiDevice") {
            bwOled1in3(null, DummySpiDevice, DummyGpio, null, 0)
        }
    }
}
