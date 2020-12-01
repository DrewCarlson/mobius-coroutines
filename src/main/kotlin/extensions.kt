package drewcarlson.mobius.flow

import com.spotify.mobius.First
import com.spotify.mobius.Next

fun <M, F> next(model: M, vararg effects: F): Next<M, F> {
    return Next.next(model, effects.toSet())
}

fun <M, F> next(model: M, effects: Set<F>): Next<M, F> {
    return Next.next(model, effects)
}

fun <M, F> dispatch(vararg effects: F): Next<M, F> {
    return Next.dispatch(effects.toSet())
}

fun <M, F> dispatch(effects: Set<F>): Next<M, F> {
    return Next.dispatch(effects)
}

fun <M, F> first(model: M, vararg effects: F): First<M, F> {
    return First.first(model, effects.toSet())
}
