package com.ndhunju.relay.api

/**
 * Encapsulates different results that an async fun can have.
 */
sealed class Result {
    data object Pending: Result()
    data class Success(val data: Any? = null): Result()
    data class Failure(val throwable: Throwable? = null): Result()

    operator fun plus(result: Result): Result {
        if ((this is Success) && (result is Success)) {
            return Success()
        }

        return Failure()
    }

    inline fun <reified T> getDataOrNull(): T? {
        if (this is Success) {
            return this.data as? T
        }

        return null
    }
}