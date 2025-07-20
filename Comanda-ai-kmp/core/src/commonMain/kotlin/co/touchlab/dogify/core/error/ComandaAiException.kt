package co.touchlab.dogify.core.error

sealed class ComandaAiException(
    open val code: String,
    override val message: String
) : Throwable(message) {

    data object NoInternetConnectionException : ComandaAiException(
        code = "10000",
        message = "No internet connection"
    )

    data class UnknownHttpException(
        val rawHttpCode: String,
        val rawHttpMessage: String
    ) : ComandaAiException(
        code = "10001",
        message = "HTTP error. Server code: $rawHttpCode; Server message: $rawHttpMessage."
    )

    data class UnknownException(
        override val message: String
    ) : ComandaAiException(
        code = "10009",
        message = "Unknown error with message: $message"
    )
}