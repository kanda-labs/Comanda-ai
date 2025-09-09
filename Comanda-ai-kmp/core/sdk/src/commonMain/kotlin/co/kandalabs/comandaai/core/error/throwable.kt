package co.kandalabs.comandaai.sdk.error

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException


internal fun Throwable.mapExceptionToComandaAi(): ComandaAiException = when (this) {
    is IOException -> ComandaAiException.NoInternetConnectionException

    is ResponseException -> {
        val status = response.status.value
        ComandaAiException.UnknownHttpException(
            rawHttpCode = status.toString(),
            rawHttpMessage = "HTTP error code: $status"
        )
    }

    else -> ComandaAiException.UnknownException(message ?: "Unexpected error")
}