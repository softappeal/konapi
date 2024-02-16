package ch.softappeal.kopi.lib

public inline fun <R> tryFinally(tryBlock: () -> R, finallyBlock: () -> Unit): R {
    var tryException: Exception? = null
    try {
        return tryBlock()
    } catch (e: Exception) {
        tryException = e
        throw tryException
    } finally {
        try {
            finallyBlock()
        } catch (finallyException: Exception) {
            if (tryException == null) throw finallyException
            tryException.addSuppressed(finallyException)
        }
    }
}

public interface Closeable {
    public fun close()
}

public inline fun <C : Closeable, R> C.use(block: C.() -> R): R = tryFinally({
    block()
}) {
    close()
}
