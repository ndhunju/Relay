package com.ndhunju.relay.api

/**
 * Encapsulates different results that an async fun can have.
 */
sealed class Result {
    data object Pending: Result()
    data class Success(val data: Any? = null): Result()
    data class Failure(val message: String? = null): Result()
}