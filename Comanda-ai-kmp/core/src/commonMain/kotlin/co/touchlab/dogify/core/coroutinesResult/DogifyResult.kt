package co.touchlab.dogify.core.coroutinesResult

import co.touchlab.dogify.core.error.ComandaAiException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class DogifyResult<out T> {

    data class Success<out T>(val data: T) : DogifyResult<T>()

    data class Failure(val exception: ComandaAiException) : DogifyResult<Nothing>()

    inline fun <R> fold(
        onSuccess: (value: T) -> R,
        onFailure: (exception: ComandaAiException) -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(exception)
    }

    inline fun onSuccess(action: (value: T) -> Unit): DogifyResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (exception: ComandaAiException) -> Unit): DogifyResult<T> {
        if (this is Failure) action(exception)
        return this
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <R> getOrElse(
        onFailure: (exception: ComandaAiException) -> R
    ): R {
        contract {
            callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
        }
        return when (this) {
            is Success -> data as R
            is Failure -> onFailure(exception)
        }
    }
}
