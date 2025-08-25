package co.kandalabs.comandaai.core.auth

import co.kandalabs.comandaai.network.HttpClientFactory
import co.kandalabs.comandaai.network.HttpClientType
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class AuthLoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthLoginResponse(
    val id: Int,
    val userName: String,
    val name: String,
    val email: String,
    val token: String,
    val role: String
)

/**
 * Manager for handling authentication operations with a dedicated coroutine scope.
 * This prevents "Parent job is Completed" errors when screen models are cancelled.
 */
object AuthManager {
    
    // Independent coroutine scope for auth operations
    private val authScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    /**
     * Performs login operation safely in an independent coroutine scope with its own HttpClient.
     * This prevents issues with cancelled screen model scopes and reused HttpClient instances.
     */
    suspend fun performLogin(
        username: String,
        password: String,
        baseUrl: String
    ): AuthLoginResponse {
        return HttpClientFactory.withSafeHttpClient(HttpClientType.AUTH) { httpClient ->
            println("AuthManager: Starting login process for user: $username...")
            println("AuthManager: Using baseUrl: $baseUrl")
            
            val request = AuthLoginRequest(username = username, password = password)
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<AuthLoginResponse>()
            
            println("AuthManager: Login process completed successfully for user: ${response.userName}")
            response
        }
    }
    
    /**
     * Performs any auth-related operation safely in an independent coroutine scope.
     */
    fun performAuthOperation(operation: suspend () -> Unit) {
        authScope.launch {
            try {
                println("AuthManager: Starting auth operation...")
                operation()
                println("AuthManager: Auth operation completed successfully")
            } catch (e: Exception) {
                println("AuthManager: Auth operation failed with error: ${e.message}")
                // Log error but don't re-throw in fire-and-forget operations
            }
        }
    }
}