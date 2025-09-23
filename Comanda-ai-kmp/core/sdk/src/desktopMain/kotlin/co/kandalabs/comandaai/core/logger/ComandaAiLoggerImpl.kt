package co.kandalabs.comandaai.core.logger

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun createPlatformLogger(): ComandaAiLogger = DesktopLogger()

private class DesktopLogger : ComandaAiLogger {
    private val defaultTag = "ComandaAi"
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    override fun e(error: Throwable, message: String) {
        log("ERROR", defaultTag, message, error)
    }

    override fun e(tag: String, error: Throwable, message: String) {
        log("ERROR", tag, message, error)
    }

    override fun d(message: String) {
        log("DEBUG", defaultTag, message)
    }

    override fun d(tag: String, message: String) {
        log("DEBUG", tag, message)
    }

    override fun i(message: String) {
        log("INFO", defaultTag, message)
    }

    override fun i(tag: String, message: String) {
        log("INFO", tag, message)
    }

    override fun w(message: String) {
        log("WARN", defaultTag, message)
    }

    override fun w(tag: String, message: String) {
        log("WARN", tag, message)
    }

    override fun v(message: String) {
        log("VERBOSE", defaultTag, message)
    }

    override fun v(tag: String, message: String) {
        log("VERBOSE", tag, message)
    }

    private fun log(level: String, tag: String, message: String, error: Throwable? = null) {
        val timestamp = LocalDateTime.now().format(formatter)
        val logMessage = "[$timestamp] [$level] [$tag] $message"

        when (level) {
            "ERROR" -> System.err.println(logMessage)
            else -> println(logMessage)
        }

        error?.printStackTrace()
    }
}