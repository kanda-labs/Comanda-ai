package co.kandalabs.comandaai.features.attendance.domain.usecases

import co.kandalabs.comandaai.features.attendance.domain.models.model.PaginatedResponse
import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.repository.UserRepository

class GetAllUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(page: Int = 1, size: Int = 20): Result<PaginatedResponse<User>> {
        return userRepository.getAllUsers(page, size)
    }
}