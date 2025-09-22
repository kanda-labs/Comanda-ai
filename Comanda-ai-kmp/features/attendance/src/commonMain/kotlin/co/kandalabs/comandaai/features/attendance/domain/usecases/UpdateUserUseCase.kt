package co.kandalabs.comandaai.features.attendance.domain.usecases

import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.models.request.UpdateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.repository.UserRepository

class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int, request: UpdateUserRequest): Result<User> {
        return userRepository.updateUser(id, request)
    }
}