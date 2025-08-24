package co.kandalabs.comandaai.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val id: Int,
    val name: String,
    val userName: String,
    val email: String?,
    val role: String,
    val token: String
)