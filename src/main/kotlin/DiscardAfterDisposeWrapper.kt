package drewcarlson.mobius.flow

import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.Disposable
import com.spotify.mobius.functions.Consumer


/**
 * Wraps a [Connection] or a [Consumer] and blocks them from receiving any
 * further values after the wrapper has been disposed.
 */
internal class DiscardAfterDisposeWrapper<I> private constructor(
        private val consumer: Consumer<I>,
        private val disposable: Disposable?
) : Consumer<I>, Disposable {

    @Volatile
    private var disposed: Boolean = false

    override fun accept(effect: I) {
        if (disposed) {
            return
        }
        consumer.accept(effect)
    }

    override fun dispose() {
        disposed = true
        disposable?.dispose()
    }

    companion object {
        fun <I> wrapConnection(connection: Connection<I>): DiscardAfterDisposeWrapper<I> {
            return DiscardAfterDisposeWrapper(connection, connection)
        }

        fun <I> wrapConsumer(consumer: Consumer<I>): DiscardAfterDisposeWrapper<I> {
            return DiscardAfterDisposeWrapper(consumer, null)
        }
    }
}