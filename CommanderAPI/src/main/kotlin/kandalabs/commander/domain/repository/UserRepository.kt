package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.User

/**
 * Repository interface for User entity.
 * Defines the contract for data access operations without implementation details.
 */
interface UserRepository {
    suspend fun findAll(): List<User>
    suspend fun findById(id: Int): User?
    suspend fun findByName(name: String): List<User>
    suspend fun findByUserName(userName: String): User?
    suspend fun create(user: User, password: String): User
    suspend fun update(id: Int, user: User): User?
    suspend fun delete(id: Int): Boolean
    
    // Add pagination support
    suspend fun findAllPaginated(page: Int, size: Int): List<User>
    suspend fun count(): Long

    // Authentication support
    suspend fun validateCredentials(userName: String, password: String): User?
}

