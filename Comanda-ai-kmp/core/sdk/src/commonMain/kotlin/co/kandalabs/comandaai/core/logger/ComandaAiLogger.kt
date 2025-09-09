package co.kandalabs.comandaai.sdk.logger

interface ComandaAiLogger {
    fun e(error: Throwable, message: String)
    fun d(message: String)
}

//TODO(evaluate using Kermit)
class ComandaAiLoggerImpl : ComandaAiLogger {
    override fun e(error: Throwable, message: String) {}
    override fun d(message: String) {}
}