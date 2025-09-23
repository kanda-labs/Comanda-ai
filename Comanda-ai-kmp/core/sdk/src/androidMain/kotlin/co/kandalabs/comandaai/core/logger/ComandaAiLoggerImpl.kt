package co.kandalabs.comandaai.core.logger

import android.util.Log

actual fun createPlatformLogger(): ComandaAiLogger = AndroidLogger()

private class AndroidLogger : ComandaAiLogger {
    private val defaultTag = "ComandaAi"

    override fun e(error: Throwable, message: String) {
        Log.e(defaultTag, message, error)
    }

    override fun e(tag: String, error: Throwable, message: String) {
        Log.e(tag, message, error)
    }

    override fun d(message: String) {
        Log.d(defaultTag, message)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(message: String) {
        Log.i(defaultTag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(message: String) {
        Log.w(defaultTag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun v(message: String) {
        Log.v(defaultTag, message)
    }

    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }
}