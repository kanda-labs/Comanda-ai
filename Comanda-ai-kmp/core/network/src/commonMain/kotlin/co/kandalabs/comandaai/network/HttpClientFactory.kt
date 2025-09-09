package co.kandalabs.comandaai.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Factory for creating HttpClient instances that prevent "Parent job is Completed" errors.
 * This creates fresh, independent HttpClients for critical operations.
 */
object HttpClientFactory {
    
    // Independent coroutine scope for HTTP operations
    private val httpScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    /**
     * Creates a new HttpClient for general API operations.
     * Each client is independent and won't be affected by cancelled parent scopes.
     */
    fun createApiClient(logPrefix: String = "HTTP"): HttpClient {
        return HttpClient {
            defaultRequest {
                header("Accept-Encoding", "identity")
                header("Content-Type", "application/json")
                header("Accept", "application/json")
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("$logPrefix: $message")
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    
    /**
     * Creates a new HttpClient for authentication operations.
     * Optimized for login/auth flows with minimal dependencies.
     */
    fun createAuthClient(): HttpClient {
        return HttpClient {
            defaultRequest {
                header("Accept-Encoding", "identity")
                header("Content-Type", "application/json")
                header("Accept", "application/json")
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("AUTH_HTTP: $message")
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    
    /**
     * Creates a new HttpClient for SSE (Server-Sent Events) operations.
     * Configured for long-lived connections.
     */
    fun createSseClient(): HttpClient {
        return HttpClient {
            defaultRequest {
                header("Accept", "text/event-stream")
                header("Cache-Control", "no-cache")
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("SSE_HTTP: $message")
                    }
                }
                level = LogLevel.INFO // Less verbose for SSE
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    
    /**
     * Executes an HTTP operation safely in an independent coroutine scope.
     * This prevents "Parent job is Completed" errors.
     */
    suspend fun <T> withSafeHttpClient(
        clientType: HttpClientType = HttpClientType.API,
        operation: suspend (HttpClient) -> T
    ): T {
        return withContext(httpScope.coroutineContext) {
            val httpClient = when (clientType) {
                HttpClientType.API -> createApiClient()
                HttpClientType.AUTH -> createAuthClient()
                HttpClientType.SSE -> createSseClient()
            }
            
            try {
                println("HttpClientFactory: Starting ${clientType.name} operation with context: ${kotlin.coroutines.coroutineContext}")
                operation(httpClient)
            } finally {
                // Always close the HttpClient to prevent resource leaks
                try {
                    httpClient.close()
                    println("HttpClientFactory: ${clientType.name} HttpClient closed successfully")
                } catch (e: Exception) {
                    println("HttpClientFactory: Error closing ${clientType.name} HttpClient: ${e.message}")
                }
            }
        }
    }
}

enum class HttpClientType {
    API, AUTH, SSE
}