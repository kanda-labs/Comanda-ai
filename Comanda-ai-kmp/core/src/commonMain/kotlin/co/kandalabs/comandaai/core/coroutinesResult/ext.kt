package co.kandalabs.comandaai.core.coroutinesResult

import co.kandalabs.comandaai.core.error.mapExceptionToComandaAi
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

suspend fun <T> safeRunCatching(
    block: suspend () -> T
): ComandaAiResult<T> {
    return try {
        ComandaAiResult.Success(block())
    } catch (error: Exception) {
        when (error) {
            is CancellationException,
            is TimeoutCancellationException -> throw error
            else -> {
                val comandaAiException = error.mapExceptionToComandaAi()
                ComandaAiResult.Failure(comandaAiException)
            }
        }
    }
}