package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.User
import kandalabs.commander.domain.repository.UserRepository
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Service class implementing the business logic for User operations.
 * Acts as a mediator between the repository and presentation layers.
 */
class UserService(private val userRepository: UserRepository) {
    
    suspend fun getAllUsers(): List<User> {
        logger.info { "Getting all users" }
        return userRepository.findAll()
    }
    
    suspend fun getUserById(id: Int): User? {
        logger.info { "Getting user by id: $id" }
        return userRepository.findById(id)
    }
    
    suspend fun searchUsersByName(name: String): List<User> {
        logger.info { "Searching users by name: $name" }
        return userRepository.findByName(name)
    }
    
    suspend fun createUser(user: User, password: String): Result<User> {
        logger.info { "Creating new user with name: ${user.name}" }
        return try {
            validateUserForCreation(user, password)
            Result.success(userRepository.create(user, password))
        } catch (e: Exception) {
            logger.error(e) { "Failed to create user: ${e.message}" }
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(id: Int, user: User): Result<User?> {
        logger.info { "Updating user with id: $id" }
        return try {
            validateUserForUpdate(user)
            val updatedUser = userRepository.update(id, user)
            if (updatedUser != null) {
                Result.success(updatedUser)
            } else {
                Result.failure(NoSuchElementException("User with id $id not found"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to update user: ${e.message}" }
            Result.failure(e)
        }
    }
    
    suspend fun deleteUser(id: Int): Result<Boolean> {
        logger.info { "Deleting user with id: $id" }
        return try {
            val result = userRepository.delete(id)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(NoSuchElementException("User with id $id not found"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete user: ${e.message}" }
            Result.failure(e)
        }
    }
    
    suspend fun getPaginatedUsers(page: Int, size: Int): Result<Pair<List<User>, Long>> {
        logger.info { "Getting paginated users - page: $page, size: $size" }
        return try {
            val validPage = if (page < 1) 1 else page
            val validSize = when {
                size < 1 -> 10
                size > 100 -> 100
                else -> size
            }

            val users = userRepository.findAllPaginated(validPage, validSize)
            val totalCount = userRepository.count()
            Result.success(Pair(users, totalCount))
        } catch (e: Exception) {
            logger.error(e) { "Failed to get paginated users: ${e.message}" }
            Result.failure(e)
        }
    }

    suspend fun validateCredentials(userName: String, password: String): User? {
        logger.info { "Validating credentials for userName: $userName" }
        return try {
            userRepository.validateCredentials(userName, password)
        } catch (e: Exception) {
            logger.error(e) { "Failed to validate credentials: ${e.message}" }
            null
        }
    }
    
    private fun validateUserForCreation(user: User, password: String) {
        require(user.name.isNotBlank()) { "User name cannot be blank" }
        require(user.userName.isNotBlank()) { "User userName cannot be blank" }
        require(password.isNotBlank()) { "Password cannot be blank" }
        require(password.length >= 6) { "Password must be at least 6 characters long" }
        if (user.email != null) {
            require(isValidEmail(user.email)) { "Invalid email format" }
        }
    }
    
    private fun validateUserForUpdate(user: User) {
        require(user.name.isNotBlank()) { "User name cannot be blank" }
        if (user.email != null) {
            require(isValidEmail(user.email)) { "Invalid email format" }
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        // Simple email validation
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        return email.matches(emailRegex)
    }
}

