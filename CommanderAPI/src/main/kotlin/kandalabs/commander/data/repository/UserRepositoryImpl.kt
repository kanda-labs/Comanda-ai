package kandalabs.commander.data.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kandalabs.commander.domain.model.User
import kandalabs.commander.domain.repository.UserRepository
import kandalabs.commander.data.model.sqlModels.UserTable
import kandalabs.commander.domain.enums.UserRole
import mu.KLogger
import org.jetbrains.exposed.sql.selectAll
import toEpochMilliseconds


/**
 * Database table definition for users
 */


/**
 * Implementation of the UserRepository interface using Exposed SQL framework
 */
class UserRepositoryImpl(
    private val userTable: UserTable,
    private val logger: KLogger
) : UserRepository {
    
    override suspend fun findAll(): List<User> {
        logger.debug { "Finding all users" }
        return transaction {
            userTable.selectAll()
                .map { it.toUser() }
        }
    }
    
    override suspend fun findById(id: Int): User? {
        logger.debug { "Finding user by id: $id" }
        return transaction {
            userTable.selectAll().where { userTable.id eq id }
                .map { it.toUser() }
                .singleOrNull()
        }
    }
    
    override suspend fun findByName(name: String): List<User> {
        logger.debug { "Finding users with name containing: $name" }
        return transaction {
            userTable.selectAll().where { userTable.name like "%$name%" }
                .map { it.toUser() }
        }
    }
    
    override suspend fun findByUserName(userName: String): User? {
        logger.debug { "Finding user by userName: $userName" }
        return transaction {
            userTable.selectAll().where { userTable.userName eq userName }
                .map { it.toUser() }
                .singleOrNull()
        }
    }
    
    override suspend fun create(user: User): User {
        logger.debug { "Creating new user: $user" }
        return transaction {
            val insertStatement = userTable.insert {
                it[name] = user.name
                it[userName] = user.userName
                it[email] = user.email
                it[active] = user.active
                it[createdAt] = user.createdAt.toEpochMilliseconds()
                it[role] = user.role.name
            }
            
            val generatedId = insertStatement[userTable.id]
            user.copy(id = generatedId)
        }
    }
    
    override suspend fun update(id: Int, user: User): User? {
        logger.debug { "Updating user with id: $id" }
        return transaction {
            val rowsUpdated = userTable.update({ userTable.id eq id }) {
                it[name] = user.name
                it[userName] = user.userName
                it[email] = user.email
                it[active] = user.active
                it[role] = user.role.name
                // Don't update createdAt as it should be immutable
            }
            
            if (rowsUpdated > 0) {
                userTable.selectAll().where { userTable.id eq id }
                    .map { it.toUser() }
                    .singleOrNull()
            } else {
                null
            }
        }
    }
    
    override suspend fun delete(id: Int): Boolean {
        logger.debug { "Deleting user with id: $id" }
        return transaction {
            userTable.deleteWhere { userTable.id eq id } > 0
        }
    }
    
    override suspend fun findAllPaginated(page: Int, size: Int): List<User> {
        logger.debug { "Finding paginated users - page: $page, size: $size" }
        return transaction {
            userTable.selectAll()
                .orderBy(userTable.id)
                .limit(size, offset = ((page - 1) * size).toLong())
                .map { it.toUser() }
        }
    }
    
    override suspend fun count(): Long {
        logger.debug { "Counting total users" }
        return transaction {
            userTable.selectAll().count()
        }
    }
    
    /**
     * Maps a database row to a domain User entity
     */
    private fun ResultRow.toUser(): User {
        return User(
            id = this[userTable.id],
            name = this[userTable.name],
            userName = this[userTable.userName],
            email = this[userTable.email],
            active = this[userTable.active],
            createdAt = this[userTable.createdAt].toLocalDateTime(),
            role = UserRole.valueOf(this[userTable.role])
        )
    }
}

