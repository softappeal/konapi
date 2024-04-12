package ch.softappeal.kopi.devices

import ch.softappeal.kopi.DummyGpio
import ch.softappeal.kopi.DummyI2cDevice
import ch.softappeal.kopi.DummySpiDevice
import ch.softappeal.kopi.GPIO_DISPLAY_DC
import ch.softappeal.kopi.GPIO_DISPLAY_RST
import ch.softappeal.kopi.Gpio
import ch.softappeal.kopi.I2C_ADDRESS_OLED
import ch.softappeal.kopi.assertFailsMessage
import ch.softappeal.kopi.devices.waveshare.bwOled1in3
import ch.softappeal.kopi.devices.waveshare.color16Oled1in5
import ch.softappeal.kopi.graphics.BLACK
import ch.softappeal.kopi.graphics.BLUE
import ch.softappeal.kopi.graphics.CYAN
import ch.softappeal.kopi.graphics.Color
import ch.softappeal.kopi.graphics.GREEN
import ch.softappeal.kopi.graphics.Graphics
import ch.softappeal.kopi.graphics.MAGENTA
import ch.softappeal.kopi.graphics.RED
import ch.softappeal.kopi.graphics.WHITE
import ch.softappeal.kopi.graphics.YELLOW
import ch.softappeal.kopi.graphics.fillRect
import ch.softappeal.kopi.i2cBus1
import ch.softappeal.kopi.spiDeviceBus0CS0
import ch.softappeal.kopi.spiDeviceBus0CS1
import ch.softappeal.kopi.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertSame
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

private suspend fun Graphics.test() {
    assertFails { color }
    assertFails { setPixel(0, 0) }
    setColor(BLACK)
    assertSame(BLACK, color)
    fillRect()
    var x = 0
    val colors = listOf(BLUE, GREEN, RED, BLACK, WHITE, CYAN, MAGENTA, YELLOW)
    val stripes = 8
    val w = width / colors.size / stripes
    colors.forEach { color ->
        repeat(stripes) { s ->
            fun map(color: UByte) = (color.toInt() / (s + 1)).toUByte()
            setColor(Color(map(color.red), map(color.green), map(color.blue))).fillRect(x, 0, w, height)
            x += w
        }
        println("${measureTime { update() }}")
        delay(1.seconds)
    }
    setColor(BLACK).fillRect()
    setColor(RED).setPixel(0, 0)
    setColor(YELLOW).setPixel(width - 1, 0)
    setColor(GREEN).setPixel(0, height - 1)
    setColor(WHITE).setPixel(width - 1, height - 1)
    setColor(BLUE).setPixel(20, 10)
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
        assertFailsMessage<IllegalStateException>("one of i2cDevice or spiDevice must be null") {
            bwOled1in3(null, null, DummyGpio, 0, 0)
        }
        assertFailsMessage<IllegalStateException>("one of i2cDevice or spiDevice must be null") {
            bwOled1in3(DummyI2cDevice, DummySpiDevice, DummyGpio, 0, 0)
        }
        assertFailsMessage<IllegalStateException>("specify dcPin only for spiDevice") {
            bwOled1in3(DummyI2cDevice, null, DummyGpio, 0, 0)
        }
        assertFailsMessage<IllegalStateException>("specify dcPin only for spiDevice") {
            bwOled1in3(null, DummySpiDevice, DummyGpio, null, 0)
        }
    }
}
