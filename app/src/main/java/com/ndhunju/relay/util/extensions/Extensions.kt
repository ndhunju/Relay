package com.ndhunju.relay.util.extensions

import android.database.Cursor
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow

/**
 * Returns String value for passed [column] if it exists.
 * Otherwise, empty string is returned
 */
fun Cursor.getStringForColumn(column: String): String {
    val columnIndex = getColumnIndex(column)
    if (columnIndex >= 0) {
        return getString(columnIndex) ?: ""
    }

    return ""
}

/**
 * Returns String value for passed [column] if it exists.
 * Otherwise, empty string is returned
 */
fun Cursor.getLongForColumn(column: String): Long {
    val columnIndex = getColumnIndex(column)
    if (columnIndex >= 0) {
        return getLong(columnIndex)
    }

    return 0
}

/**
 * Returns a [Flow] whose values are generated with [transform] function by combining
 * the most recently emitted values by each flow.
 */
inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7) -> R
): Flow<R> =
    kotlinx.coroutines.flow.combine(flow1, flow2, flow3, flow4, flow5, flow6, flow7) { args: Array<*> ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7
        )
    }

/**
 * Returns a [Flow] whose values are generated with [transform] function by combining
 * the most recently emitted values by each flow.
 */
inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7,
        reified T8, reified T9, reified T10, reified T11, reified T12, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    flow10: Flow<T10>,
    flow11: Flow<T11>,
    flow12: Flow<T12>,
    crossinline transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R
): Flow<R> =
    kotlinx.coroutines.flow.combine(flow1, flow2, flow3, flow4, flow5, flow6, flow7,
        flow8, flow9, flow10, flow11, flow12) { args: Array<*> ->
        transform(
            args[0] as T1,
            args[1] as T2,
            args[2] as T3,
            args[3] as T4,
            args[4] as T5,
            args[5] as T6,
            args[6] as T7,
            args[7] as T8,
            args[8] as T9,
            args[9] as T10,
            args[10] as T11,
            args[11] as T12
        )
    }

/**
 * Not thread safe
 */
fun <K,V> MutableMap<K,V>.getOrPut(key: K, default: V): V {
    if (this[key] == null) {
        this[key] = default
    }

    return  this[key] ?: default
}

/**
 * Get the [MutableState] object as [State]
 * whose value can't be changed by consumer
 */
fun <T> MutableState<T>.asState(): State<T> {
    val originalState = this
    return object: State<T> {
        override val value: T
            get() = originalState.value

    }
}