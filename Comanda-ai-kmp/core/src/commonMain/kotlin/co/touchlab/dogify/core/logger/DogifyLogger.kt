package co.touchlab.dogify.core.logger

interface DogifyLogger {
    fun e(error: Throwable, message: String)
    fun d(message: String)
}

//TODO(evaluate using Kermit)
class DogifyLoggerImpl : DogifyLogger {
    override fun e(error: Throwable, message: String) {}
    override fun d(message: String) {}
}