package co.kandalabs.comandaai.features.attendance.domain.usecases

import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.repository.UserRepository

class GetUserByIdUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: Int): Result<User> {
        return userRepository.getUserById(id)
    }
}