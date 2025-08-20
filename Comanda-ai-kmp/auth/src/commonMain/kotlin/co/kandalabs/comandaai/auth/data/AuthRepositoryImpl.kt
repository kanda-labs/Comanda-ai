package co.kandalabs.comandaai.auth.data

import co.kandalabs.comandaai.auth.domain.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://192.168.0.56:8081/api/v1"
) : AuthRepository {
    
    override suspend fun login(request: LoginRequest): LoginResponse {
        return httpClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}