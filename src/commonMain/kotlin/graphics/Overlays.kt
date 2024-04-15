package ch.softappeal.kopi.graphics

import ch.softappeal.kopi.readFile

private fun ByteArray.isSet(bit: Int) = (this[bit / 8].toInt() and (1 shl (bit % 8))) != 0

public open class Overlays(width: Int, height: Int, internal val bitmap: ByteArray) : Dimensions(width, height) {
    public constructor(overlays: Overlays) : this(overlays.width, overlays.height, overlays.bitmap)

    private val bits = width * height
    public fun draw(graphics: Graphics, xTopLeft: Int, yTopLeft: Int, index: Int) {
        var bit = index * bits
        for (y in yTopLeft..<yTopLeft + height) {
            for (x in xTopLeft..<xTopLeft + width) {
                if (bitmap.isSet(bit++)) graphics.setPixel(x, y)
            }
        }
    }
}

public fun Overlays.toBytes(): ByteArray {
    val bytes = ByteArray(2 + bitmap.size)
    bytes[0] = width.toByte()
    bytes[1] = height.toByte()
    bitmap.copyInto(bytes, destinationOffset = 2)
    return bytes
}

public fun ByteArray.toOverlays(): Overlays = Overlays(
    width = this[0].toInt(),
    height = this[1].toInt(),
    bitmap = copyOfRange(2, size),
)

public fun readOverlaysFile(path: String): Overlays = readFile(path).toOverlays()
