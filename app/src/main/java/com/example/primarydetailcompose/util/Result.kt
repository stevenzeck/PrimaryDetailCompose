package com.example.primarydetailcompose.util

/**
 * A generic sealed class representing the result of an operation.
 *
 * Can be either [Success] containing data or [Error] containing an exception.
 *
 * @param R The type of data contained in the result.
 */
sealed class Result<out R> {
    /**
     * Represents a successful operation.
     *
     * @property data The result data.
     */
    data class Success<out T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation.
     *
     * @property exception The exception describing the error.
     */
    data class Error(val exception: Exception) : Result<Nothing>()
}

/**
 * Extension function to retrieve the success value or a fallback if the result is an error.
 *
 * @param fallback The value to return if the result is not [Result.Success].
 * @return The data if successful, otherwise the fallback value.
 */
fun <T> Result<T>.successOr(fallback: T): T {
    return (this as? Result.Success<T>)?.data ?: fallback
}
