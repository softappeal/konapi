package ch.softappeal.kopi.graphics

public val FONT_CHARS: CharRange = ' '..'~'

public fun Graphics.draw(xTopLeft: Int, yTopLeft: Int, ch: Char) {
    font.draw(this, xTopLeft, yTopLeft, ch - FONT_CHARS.first)
}

public fun Graphics.draw(topLeft: Point, ch: Char) {
    draw(topLeft.x, topLeft.y, ch)
}

public fun Graphics.draw(xTopLeft: Int, yTopLeft: Int, string: String) {
    var x = xTopLeft
    for (ch in string) {
        draw(x, yTopLeft, ch)
        x += font.width
    }
}

public fun Graphics.draw(topLeft: Point, string: String) {
    draw(topLeft.x, topLeft.y, string)
}
