package ch.softappeal.kopi.graphics

import ch.softappeal.kopi.readFile

private fun ByteArray.isSet(bit: Int) = (this[bit / 8].toInt() and (1 shl (bit % 8))) != 0
private fun ByteArray.set(bit: Int) {
    this[bit / 8] = (this[bit / 8].toInt() or (1 shl (bit % 8))).toByte()
}

public open class Overlays(
    internal val size: Int, width: Int, height: Int, internal val bitmap: ByteArray,
) : Dimensions(width, height) {
    public constructor(overlays: Overlays) : this(overlays.size, overlays.width, overlays.height, overlays.bitmap)

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
    bytes[0] = size.toByte()
    bytes[1] = width.toByte()
    bytes[2] = height.toByte()
    bitmap.copyInto(bytes, destinationOffset = 3)
    return bytes
}

public fun ByteArray.toOverlays(): Overlays = Overlays(
    size = this[0].toInt(),
    width = this[1].toInt(),
    height = this[2].toInt(),
    bitmap = copyOfRange(3, size),
)

public fun readOverlaysFile(path: String): Overlays = readFile(path).toOverlays()

public fun Overlays.dump(): String {
    val s = StringBuilder()
    s.append("$size\n")
    s.append("$width\n")
    s.append("$height\n")
    var bit = 0
    for (index in 0..<size) {
        s.append('\n')
        s.append("$index\n")
        repeat(height) {
            repeat(width) {
                s.append(if (bitmap.isSet(bit++)) "#" else ".")
            }
            s.append('\n')
        }
    }
    return s.toString()
}

public fun String.toOverlays(): Overlays {
    val lines = trimIndent().split("\n").iterator()
    val size = lines.next().toInt()
    val width = lines.next().toInt()
    val height = lines.next().toInt()
    val bitmap = ByteArray((size * width * height / 8) + 1)
    var bit = 0
    for (index in 0..<size) {
        check(lines.next().isEmpty()) { "missing empty line before index $index" }
        val actualIndex = lines.next().toInt()
        check(index == actualIndex) { "index $index expected (actual is $actualIndex)" }
        repeat(height) {
            val line = lines.next()
            check(width == line.length) { "wrong line width at index $index (${line.length} instead of $width)" }
            for (w in 0..<width) {
                when (val ch = line[w]) {
                    '.' -> {} // empty
                    '#' -> bitmap.set(bit)
                    else -> error("unexpected char '$ch' at index $index")
                }
                bit++
            }
        }
    }
    check(!lines.hasNext()) { "unexpected lines at end" }
    return Overlays(size, width, height, bitmap)
}
