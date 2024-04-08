package com.ndhunju.relay.api

/**
 * Encapsulates different results that an async fun can have.
 */
sealed class Result<T> {
    data class Pending<T>(val percentage: Float = 0f): Result<T>()
    data class Success<T>(val data: T? = null): Result<T>()
    data class Failure<T>(val throwable: Throwable? = null): Result<T>()

    operator fun plus(result: Result<T>): Result<T> {
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