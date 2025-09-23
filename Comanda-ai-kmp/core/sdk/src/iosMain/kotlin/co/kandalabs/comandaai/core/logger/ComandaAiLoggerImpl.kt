package co.kandalabs.comandaai.core.logger

import platform.Foundation.NSLog

actual fun createPlatformLogger(): ComandaAiLogger = IosLogger()

private class IosLogger : ComandaAiLogger {
    private val defaultTag = "ComandaAi"

    override fun e(error: Throwable, message: String) {
        NSLog("[ERROR] [$defaultTag] $message: ${error.message}")
        error.printStackTrace()
    }

    override fun e(tag: String, error: Throwable, message: String) {
        NSLog("[ERROR] [$tag] $message: ${error.message}")
        error.printStackTrace()
    }

    override fun d(message: String) {
        NSLog("[DEBUG] [$defaultTag] $message")
    }

    override fun d(tag: String, message: String) {
        NSLog("[DEBUG] [$tag] $message")
    }

    override fun i(message: String) {
        NSLog("[INFO] [$defaultTag] $message")
    }

    override fun i(tag: String, message: String) {
        NSLog("[INFO] [$tag] $message")
    }

    override fun w(message: String) {
        NSLog("[WARN] [$defaultTag] $message")
    }

    override fun w(tag: String, message: String) {
        NSLog("[WARN] [$tag] $message")
    }

    override fun v(message: String) {
        NSLog("[VERBOSE] [$defaultTag] $message")
    }

    override fun v(tag: String, message: String) {
        NSLog("[VERBOSE] [$tag] $message")
    }
}