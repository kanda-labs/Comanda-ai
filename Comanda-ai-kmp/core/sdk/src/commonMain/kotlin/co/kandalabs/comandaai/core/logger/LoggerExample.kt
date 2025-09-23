package co.kandalabs.comandaai.core.logger

import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Example of how to use the logger with Dependency Injection
 *
 * Usage in ViewModels or other classes:
 * ```kotlin
 * class MyViewModel(override val di: DI) : DIAware {
 *     private val logger: ComandaAiLogger by instance()
 *
 *     fun doSomething() {
 *         logger.d("MyViewModel", "Doing something...")
 *         try {
 *             // Some operation
 *             logger.i("MyViewModel", "Operation successful")
 *         } catch (e: Exception) {
 *             logger.e("MyViewModel", e, "Operation failed")
 *         }
 *     }
 * }
 * ```
 *
 * Or with constructor injection:
 * ```kotlin
 * class MyService(
 *     private val logger: ComandaAiLogger
 * ) {
 *     fun process() {
 *         logger.d("Processing...")
 *     }
 * }
 *
 * // In DI Module:
 * bindSingleton { MyService(instance()) }
 * ```
 */
class LoggerExampleClass(override val di: DI) : DIAware {
    private val logger: ComandaAiLogger by instance()

    fun demonstrateLogging() {
        // Verbose logging
        logger.v("This is a verbose log")
        logger.v("ExampleTag", "Verbose log with custom tag")

        // Debug logging
        logger.d("This is a debug log")
        logger.d("ExampleTag", "Debug log with custom tag")

        // Info logging
        logger.i("This is an info log")
        logger.i("ExampleTag", "Info log with custom tag")

        // Warning logging
        logger.w("This is a warning log")
        logger.w("ExampleTag", "Warning log with custom tag")

        // Error logging
        try {
            throw Exception("Example exception")
        } catch (e: Exception) {
            logger.e(e, "An error occurred")
            logger.e("ExampleTag", e, "Error with custom tag")
        }
    }
}