package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.readFile

private fun ByteArray.isSet(bit: Int) = (this[bit / 8].toInt() and (1 shl (bit % 8))) != 0
private fun ByteArray.set(bit: Int) {
    this[bit / 8] = (this[bit / 8].toInt() or (1 shl (bit % 8))).toByte()
}

public open class Overlays(dimensions: Dimensions, public val size: Int, internal val bitmap: ByteArray) : Dimensions(dimensions) {
    public constructor(overlays: Overlays) : this(overlays, overlays.size, overlays.bitmap)

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
    val bytes = ByteArray(3 + bitmap.size)
    bytes[0] = width.toByte()
    bytes[1] = height.toByte()
    bytes[2] = size.toByte()
    bitmap.copyInto(bytes, destinationOffset = 3)
    return bytes
}

public fun ByteArray.toOverlays(): Overlays =
    Overlays(Dimensions(this[0].toInt(), this[1].toInt()), this[2].toInt(), copyOfRange(3, size))

public fun readOverlaysFile(path: String): Overlays = readFile(path).toOverlays()

public fun Overlays.dump(): String {
    val s = StringBuilder()
    var bit = 0
    for (index in 0..<size) {
        s.append("$index\n")
        repeat(height) {
            repeat(width) {
                s.append(if (bitmap.isSet(bit++)) STRING_PIXEL_ON else STRING_PIXEL_OFF)
            }
            s.append('\n')
        }
    }
    return s.toString()
}

public fun Overlays(dimensions: Dimensions, size: Int, dump: String): Overlays {
    val lineWidth = dimensions.width * STRING_PIXEL_WIDTH
    val lines = dump.trimIndent().split("\n").iterator()
    val bitmap = ByteArray((size * dimensions.width * dimensions.height / 8) + 1)
    var bit = 0
    for (index in 0..<size) {
        val actualIndex = lines.next().toInt()
        check(index == actualIndex) { "index $index expected (actual is $actualIndex)" }
        repeat(dimensions.height) {
            val line = lines.next()
            check(lineWidth == line.length) { "wrong line width at index $index (${line.length} instead of $lineWidth)" }
            for (w in 0..<lineWidth step 2) {
                when (val p = line.subSequence(w, w + STRING_PIXEL_WIDTH)) {
                    STRING_PIXEL_OFF -> {} // empty
                    STRING_PIXEL_ON -> bitmap.set(bit)
                    else -> error("unexpected pixel '$p' at index $index")
                }
                bit++
            }
        }
    }
    check(!lines.hasNext()) { "unexpected lines at end" }
    return Overlays(dimensions, size, bitmap)
}
