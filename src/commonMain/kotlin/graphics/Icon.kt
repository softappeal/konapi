package ch.softappeal.konapi.graphics

public open class Icon(public val overlay: Overlay, public val index: Int) : Dimension(overlay) {
    init {
        require(index in 0..<overlay.size) { "index=$index must be in 0..<${overlay.size}" }
    }
}

public fun Graphics.draw(xTopLeft: Int, yTopLeft: Int, icon: Icon) {
    icon.overlay.draw(this, xTopLeft, yTopLeft, icon.index)
}

public fun Graphics.draw(topLeft: Point, icon: Icon) {
    draw(topLeft.x, topLeft.y, icon)
}

public abstract class Icons(public val overlay: Overlay) : Dimension(overlay)

public fun Icons.Icon(index: Int): Icon = Icon(overlay, index)

public class FontIcon(overlay: Overlay, index: Int, public val codePoint: Int) : Icon(overlay, index)

public fun Icons.FontIcon(index: Int, codePoint: Int): FontIcon = FontIcon(overlay, index, codePoint)

public val DummyOverlay: Overlay = Overlay(Int.MAX_VALUE, Dimension(1, 1), byteArrayOf())
