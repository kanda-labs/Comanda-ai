package co.touchlab.dogify.core.coroutinesResult

import co.touchlab.dogify.core.error.mapExceptionToDogify
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeRunCatching(
    block: suspend () -> T
): DogifyResult<T> {
    return try {
        DogifyResult.Success(block())
    } catch (error: Exception) {
        when (error) {
            is CancellationException,
            is TimeoutCancellationException -> throw error
            else -> {
                val dogifyException = error.mapExceptionToDogify()
                DogifyResult.Failure(dogifyException)
            }
        }
    }
}