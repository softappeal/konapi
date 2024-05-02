package sample

import ch.softappeal.konapi.devices.bosch.Bme280
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.readOverlayFile
import kotlin.math.roundToInt

private val viewFont = readOverlayFile("$filesDir/Pxl.9x21.font")

class Bme280View(graphics: Graphics, private val bme280: Bme280) : View(graphics) {
    override fun Graphics.drawImpl() {
        set(viewFont)
        val (temperaturInCelsius, pressureInPascal, humidityInPercent) = bme280.measurement()
        drawLine(0, (temperaturInCelsius * 10).roundToInt().toString() + " 0.1C")
        drawLine(1, (pressureInPascal / 100).roundToInt().toString() + " hPa")
        drawLine(2, (humidityInPercent * 10).roundToInt().toString() + " 0.1%")
    }
}
