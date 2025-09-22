package co.kandalabs.comandaai.features.attendance.domain.usecases

import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.repository.UserRepository

class CreateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(request: CreateUserRequest): Result<User> {
        // Validate input
        if (request.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Name cannot be empty"))
        }
        if (request.userName.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty"))
        }
        if (request.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        if (request.email != null && request.email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (request.email != null && !request.email.matches(Regex(".+@.+\\..+"))) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        return userRepository.createUser(request)
    }
}