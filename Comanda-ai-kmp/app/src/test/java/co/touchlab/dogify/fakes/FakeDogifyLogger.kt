package co.touchlab.dogify.fakes

import co.touchlab.dogify.core.logger.DogifyLogger

internal class FakeDogifyLogger : DogifyLogger {
    var errorMessages = mutableListOf<String>()

    override fun d(message: String) {}

    override fun e(error: Throwable, message: String) {
        errorMessages.add("Error: ${error.message} - $message")
    }
}