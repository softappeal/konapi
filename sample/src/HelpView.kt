package sample

import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.readOverlayFile
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val viewFont = readOverlayFile("$filesDir/Hd44780.6x10.font")

class HelpView(graphics: Graphics) : View(graphics) {
    override fun Graphics.drawImpl() {
        set(viewFont)
        drawLine(0, "Anti- / Clockwise ->")
        drawLine(1, "    switch view")
        drawLine(2, "Left / Right ->")
        drawLine(3, "    switch page")
        drawLine(4, "Up / Down ->")
        drawLine(5, "    switch color")
        if (height >= (font.height * 7)) drawLine(
            6, (timeSource.markNow() - booted).inWholeSeconds.toDuration(DurationUnit.SECONDS).toString()
        )
    }
}
