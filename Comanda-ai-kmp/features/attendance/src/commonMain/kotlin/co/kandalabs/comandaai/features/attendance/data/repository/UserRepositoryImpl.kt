package co.kandalabs.comandaai.features.attendance.data.repository

import co.kandalabs.comandaai.features.attendance.data.api.CommanderApi
import co.kandalabs.comandaai.features.attendance.domain.models.model.PaginatedResponse
import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.models.request.UpdateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class UserRepositoryImpl(
    private val api: CommanderApi,
    private val dispatcher: CoroutineDispatcher
) : UserRepository {

    override suspend fun createUser(request: CreateUserRequest): Result<User> {
        return withContext(dispatcher) {
            try {
                val user = api.createUser(request)
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getAllUsers(page: Int, size: Int): Result<PaginatedResponse<User>> {
        return withContext(dispatcher) {
            try {
                val response = api.getUsers(page, size)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserById(id: Int): Result<User> {
        return withContext(dispatcher) {
            try {
                val user = api.getUserById(id)
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUser(id: Int, request: UpdateUserRequest): Result<User> {
        return withContext(dispatcher) {
            try {
                val user = api.updateUser(id, request)
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteUser(id: Int): Result<Boolean> {
        return withContext(dispatcher) {
            try {
                api.deleteUser(id)
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}