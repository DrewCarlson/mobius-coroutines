package drewcarlson.mobius.flow

import com.spotify.mobius.Connectable
import com.spotify.mobius.Connection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive

/**
 * Constructs a [Connectable] that applies [transform] to
 * map a [Flow] of [I] into a [Flow] of [O].
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
fun <I, O> flowConnectable(
    transform: FlowTransformer<I, O>
) = Connectable<I, O> { consumer ->
    val scope = CoroutineScope(Dispatchers.Unconfined)
    val inputChannel = BroadcastChannel<I>(BUFFERED)
    inputChannel.asFlow()
        .run(transform)
        .takeWhile { scope.isActive }
        .onEach { output -> consumer.accept(output) }
        .launchIn(scope)
    object : Connection<I> {
        override fun accept(value: I) {
            inputChannel.offer(value)
        }

        override fun dispose() {
            scope.cancel()
        }
    }
}

/**
 * Transforms the [FlowTransformer] into a [Connectable].
 */
fun <I, O> FlowTransformer<I, O>.asConnectable(): Connectable<I, O> =
    flowConnectable(this)

/**
 * Transforms a [Flow] of [I] into a [Flow] of [O] using
 * the provided [Connectable] [connectable].
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <I, O> Flow<I>.transform(
    connectable: Connectable<I, O>
): Flow<O> = callbackFlow {
    val connection = connectable.connect { output ->
        offer(output)
    }
    onEach { input ->
        connection.accept(input)
    }.onCompletion {
        close()
    }.launchIn(this)

    awaitClose {
        connection.dispose()
    }
}
