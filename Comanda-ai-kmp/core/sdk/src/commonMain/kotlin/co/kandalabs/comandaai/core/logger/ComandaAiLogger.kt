package co.kandalabs.comandaai.core.logger

interface ComandaAiLogger {
    fun e(error: Throwable, message: String)
    fun e(tag: String, error: Throwable, message: String)
    fun d(message: String)
    fun d(tag: String, message: String)
    fun i(message: String)
    fun i(tag: String, message: String)
    fun w(message: String)
    fun w(tag: String, message: String)
    fun v(message: String)
    fun v(tag: String, message: String)
}

expect fun createPlatformLogger(): ComandaAiLogger

object LoggerFactory {
    private var logger: ComandaAiLogger? = null

    fun getLogger(): ComandaAiLogger {
        return logger ?: createPlatformLogger().also { logger = it }
    }

    fun setLogger(customLogger: ComandaAiLogger) {
        logger = customLogger
    }
}