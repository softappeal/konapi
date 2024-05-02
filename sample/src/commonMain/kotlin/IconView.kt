package sample

import ch.softappeal.konapi.graphics.Dimension
import ch.softappeal.konapi.graphics.FontIcon
import ch.softappeal.konapi.graphics.Graphics
import ch.softappeal.konapi.graphics.Icon
import ch.softappeal.konapi.graphics.Icons
import ch.softappeal.konapi.graphics.Overlay
import ch.softappeal.konapi.graphics.draw
import ch.softappeal.konapi.graphics.readOverlayFile

private object StringIcons : Icons(Overlay(2, Dimension(8, 8), """
    0
    ########........
    ########........
    ########........
    ########........
    ........########
    ........########
    ........########
    ........########
    1
    ........########
    ........########
    ........########
    ........########
    ########........
    ########........
    ########........
    ########........
""")) {
    val topLeftToBottomRight = Icon(0)
    val topRightToBottomLeft = Icon(1)
}

private abstract class FontIcons(overlay: Overlay) : Icons(overlay) {
    val handSpock = FontIcon(0, 0x1F596)
    val addressCard = FontIcon(1, 0xF2BB)
}

private object FontIconsSmall : FontIcons(readOverlayFile("$filesDir/icons.18x18.overlay"))
private object FontIconsBig : FontIcons(readOverlayFile("$filesDir/icons.36x34.overlay"))

private val icons = listOf(
    Pair(StringIcons.topLeftToBottomRight, StringIcons.topRightToBottomLeft),
    Pair(FontIconsSmall.handSpock, FontIconsSmall.addressCard),
    Pair(FontIconsBig.handSpock, FontIconsBig.addressCard),
)

class IconView(graphics: Graphics) : View(graphics) {
    private var iconIndex = 0

    override fun nextPage() {
        if (++iconIndex >= icons.size) iconIndex = 0
    }

    override fun prevPage() {
        if (--iconIndex < 0) iconIndex = icons.size - 1
    }

    override fun Graphics.drawImpl() {
        val pair = icons[iconIndex]
        draw(0, 0, pair.first)
        draw(width - pair.second.width, height - pair.second.height, pair.second)
    }
}
