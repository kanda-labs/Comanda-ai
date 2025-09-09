package co.kandalabs.comandaai.auth.domain

import co.kandalabs.comandaai.auth.data.LoginRequest
import co.kandalabs.comandaai.auth.data.LoginResponse

interface AuthRepository {
    suspend fun login(request: LoginRequest): LoginResponse
}