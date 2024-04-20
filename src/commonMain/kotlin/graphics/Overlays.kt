package ch.softappeal.konapi.graphics

import ch.softappeal.konapi.readFile

private fun ByteArray.isSet(bit: Int) = (this[bit / 8].toInt() and (1 shl (bit % 8))) != 0
private fun ByteArray.set(bit: Int) {
    this[bit / 8] = (this[bit / 8].toInt() or (1 shl (bit % 8))).toByte()
}

public open class Overlays internal constructor(
    public val size: Int, dimensions: Dimensions, internal val bitmap: ByteArray,
) : Dimensions(dimensions) {
    init {
        require(size >= 0) { "size=$size must >= 0" }
    }

    public constructor(overlays: Overlays) : this(overlays.size, overlays, overlays.bitmap)

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

private const val OVERLAYS_MAGIC_NUMBER = 0xA9.toByte()
private const val OVERLAYS_VERSION_1 = 1.toByte()

public fun Overlays.toBytes(): ByteArray {
    val bytes = ByteArray(5 + bitmap.size)
    bytes[0] = OVERLAYS_MAGIC_NUMBER
    bytes[1] = OVERLAYS_VERSION_1
    bytes[2] = size.toByte()
    bytes[3] = width.toByte()
    bytes[4] = height.toByte()
    bitmap.copyInto(bytes, destinationOffset = 5)
    return bytes
}

public fun ByteArray.toOverlays(): Overlays {
    require(this[0] == OVERLAYS_MAGIC_NUMBER) { "overlays has invalid magic number" }
    require(this[1] == OVERLAYS_VERSION_1) { "overlays must have version $OVERLAYS_VERSION_1" }
    return Overlays(this[2].toInt(), Dimensions(this[3].toInt(), this[4].toInt()), copyOfRange(5, size))
}

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

public fun Overlays(size: Int, dimensions: Dimensions, dump: String): Overlays {
    val lineWidth = dimensions.width * STRING_PIXEL_WIDTH
    val lines = dump.trimIndent().split("\n").iterator()
    val bitmap = ByteArray((size * dimensions.width * dimensions.height / 8) + 1)
    var bit = 0
    for (index in 0..<size) {
        val actualIndex = lines.next().toInt()
        require(index == actualIndex) { "index $index expected (actual is $actualIndex)" }
        repeat(dimensions.height) {
            val line = lines.next()
            require(lineWidth == line.length) { "wrong line width at index $index (${line.length} instead of $lineWidth)" }
            for (w in 0..<lineWidth step 2) {
                when (val p = line.subSequence(w, w + STRING_PIXEL_WIDTH)) {
                    STRING_PIXEL_OFF -> {} // empty
                    STRING_PIXEL_ON -> bitmap.set(bit)
                    else -> throw IllegalArgumentException("unexpected pixel '$p' at index $index")
                }
                bit++
            }
        }
    }
    require(!lines.hasNext()) { "unexpected lines at end" }
    return Overlays(size, dimensions, bitmap)
}
