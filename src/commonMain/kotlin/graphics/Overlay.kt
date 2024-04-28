package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.readFile

private fun ByteArray.isSet(bit: Int) = (this[bit / 8].toInt() and (1 shl (bit % 8))) != 0
private fun ByteArray.set(bit: Int) {
    this[bit / 8] = (this[bit / 8].toInt() or (1 shl (bit % 8))).toByte()
}

public open class Overlay internal constructor(
    public val size: Int, dimension: Dimension, internal val bitmap: ByteArray,
) : Dimension(dimension) {
    init {
        require(size >= 0) { "size=$size must >= 0" }
    }

    private val bits = width * height
    public fun draw(graphics: Graphics, xTopLeft: Int, yTopLeft: Int, index: Int) {
        require(index in 0..<size) { "index=$index must be in 0..<$size" }
        var bit = index * bits
        for (y in yTopLeft..<yTopLeft + height) {
            for (x in xTopLeft..<xTopLeft + width) {
                if (bitmap.isSet(bit++)) graphics.setPixel(x, y)
            }
        }
    }
}

private const val OVERLAY_MAGIC_NUMBER = 0xA9.toByte()
private const val OVERLAY_VERSION_1 = 1.toByte()

public fun Overlay.toBytes(): ByteArray {
    val bytes = ByteArray(5 + bitmap.size)
    bytes[0] = OVERLAY_MAGIC_NUMBER
    bytes[1] = OVERLAY_VERSION_1
    bytes[2] = size.toByte()
    bytes[3] = width.toByte()
    bytes[4] = height.toByte()
    bitmap.copyInto(bytes, destinationOffset = 5)
    return bytes
}

public fun ByteArray.toOverlay(): Overlay {
    require(this[0] == OVERLAY_MAGIC_NUMBER) { "overlay has invalid magic number" }
    require(this[1] == OVERLAY_VERSION_1) { "overlay must have version $OVERLAY_VERSION_1" }
    return Overlay(this[2].toInt(), Dimension(this[3].toInt(), this[4].toInt()), copyOfRange(5, size))
}

public fun readOverlayFile(path: String): Overlay = readFile(path).toOverlay()

public const val DUMP_PIXEL_WIDTH: Int = 2 // needed for near quadratic output
public const val DUMP_PIXEL_ON: String = "##"
public const val DUMP_PIXEL_OFF: String = ".."

public fun Overlay.dump(): String {
    val s = StringBuilder()
    var bit = 0
    for (index in 0..<size) {
        s.append("$index\n")
        repeat(height) {
            repeat(width) {
                s.append(if (bitmap.isSet(bit++)) DUMP_PIXEL_ON else DUMP_PIXEL_OFF)
            }
            s.append('\n')
        }
    }
    return s.toString()
}

public fun Overlay(size: Int, dimension: Dimension, dump: String): Overlay {
    val lineWidth = dimension.width * DUMP_PIXEL_WIDTH
    val lines = dump.trimIndent().split("\n").iterator()
    val bitmap = ByteArray((size * dimension.width * dimension.height / 8) + 1)
    var bit = 0
    for (index in 0..<size) {
        val actualIndex = lines.next().toInt()
        require(index == actualIndex) { "index $index expected (actual is $actualIndex)" }
        repeat(dimension.height) {
            val line = lines.next()
            require(lineWidth == line.length) { "wrong line width at index $index (${line.length} instead of $lineWidth)" }
            for (w in 0..<lineWidth step DUMP_PIXEL_WIDTH) {
                when (val p = line.subSequence(w, w + DUMP_PIXEL_WIDTH)) {
                    DUMP_PIXEL_OFF -> {} // empty
                    DUMP_PIXEL_ON -> bitmap.set(bit)
                    else -> throw IllegalArgumentException("unexpected pixel '$p' at index $index")
                }
                bit++
            }
        }
    }
    require(!lines.hasNext()) { "unexpected lines at end" }
    return Overlay(size, dimension, bitmap)
}
