package co.kandalabs.comandaai.core.logger

interface ComandaAiLogger {
    fun e(error: Throwable, message: String)
    fun d(message: String)
}

//TODO(evaluate using Kermit)
class ComandaAiLoggerImpl : ComandaAiLogger {
    override fun e(error: Throwable, message: String) {}
    override fun d(message: String) {}
}