package ch.softappeal.konapi

public inline fun <R> tryFinally(tryBlock: () -> R, finallyBlock: () -> Unit): R {
    val result = try {
        tryBlock()
    } catch (tryException: Exception) {
        try {
            finallyBlock()
        } catch (finallyException: Exception) {
            tryException.addSuppressed(finallyException)
        }
        throw tryException
    }
    finallyBlock()
    return result
}

public interface Closeable {
    public fun close()
}

public inline fun <C : Closeable, R> C.use(block: (closeable: C) -> R): R = tryFinally({
    block(this)
}) {
    close()
}

public inline fun <R> tryCatch(tryBlock: () -> R, catchBlock: () -> Unit): R = try {
    tryBlock()
} catch (tryException: Exception) {
    try {
        catchBlock()
    } catch (catchException: Exception) {
        tryException.addSuppressed(catchException)
    }
    throw tryException
}
