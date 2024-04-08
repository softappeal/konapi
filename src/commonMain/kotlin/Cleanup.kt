package ch.softappeal.kopi

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

public interface SuspendCloseable {
    public suspend fun close()
}

public inline fun <C : Closeable, R> C.use(block: (closeable: C) -> R): R = tryFinally({
    block(this)
}) {
    close()
}

public suspend inline fun <C : SuspendCloseable, R> C.use(block: (closeable: C) -> R): R = tryFinally({
    block(this)
}) {
    close()
}

public inline fun <R> tryCatch(tryBlock: () -> R, catchBlock: () -> Unit): R {
    try {
        return tryBlock()
    } catch (tryException: Exception) {
        try {
            catchBlock()
        } catch (catchException: Exception) {
            tryException.addSuppressed(catchException)
        }
        throw tryException
    }
}
