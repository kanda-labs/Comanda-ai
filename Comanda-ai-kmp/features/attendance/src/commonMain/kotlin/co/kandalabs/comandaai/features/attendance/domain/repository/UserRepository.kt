package co.kandalabs.comandaai.features.attendance.domain.repository

import co.kandalabs.comandaai.features.attendance.domain.models.model.PaginatedResponse
import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.models.request.UpdateUserRequest

interface UserRepository {
    suspend fun createUser(request: CreateUserRequest): Result<User>
    suspend fun getAllUsers(page: Int = 1, size: Int = 20): Result<PaginatedResponse<User>>
    suspend fun getUserById(id: Int): Result<User>
    suspend fun updateUser(id: Int, request: UpdateUserRequest): Result<User>
    suspend fun deleteUser(id: Int): Result<Boolean>
}