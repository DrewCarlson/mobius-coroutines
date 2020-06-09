package drewcarlson.mobius.flow

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import com.spotify.mobius.disposables.CompositeDisposable
import com.spotify.mobius.functions.Consumer
import drewcarlson.mobius.flow.DiscardAfterDisposeWrapper.Companion.wrapConnection
import drewcarlson.mobius.flow.DiscardAfterDisposeWrapper.Companion.wrapConsumer


/**
 * A [Connectable] that ensures that [Connection]s created by the wrapped
 * [Connectable] don't emit or receive any values after being disposed.
 *
 * This only acts as a safeguard, you still need to make sure that the
 * Connectable disposes of resources correctly.
 */
class DiscardAfterDisposeConnectable<I, O>(
        private val actual: Connectable<I, O>
) : Connectable<I, O> {

    override fun connect(output: Consumer<O>): Connection<I> {
        val safeOutput = wrapConsumer(output)
        val input = actual.connect(safeOutput)
        val safeInput = wrapConnection(input)
        val disposable = CompositeDisposable.from(safeInput, safeOutput)
        return object : Connection<I> {
            override fun accept(effect: I) {
                safeInput.accept(effect)
            }

            override fun dispose() {
                disposable.dispose()
            }
        }
    }
}