package kandalabs.commander.infrastructure.persistence

import kandalabs.commander.data.model.sqlModels.UserTable
import kandalabs.commander.data.repository.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import kandalabs.commander.domain.model.User
import kotlin.test.*

class UserRepositoryImplTest {
    private lateinit var repository: UserRepositoryImpl
    
    @BeforeEach
    fun setup() {
        // Setup in-memory database for testing
        Database.connect("jdbc:sqlite:file:test?mode=memory&cache=shared", driver = "org.sqlite.JDBC")
        
        // Create tables
        transaction {
            SchemaUtils.drop(UserTable)
            SchemaUtils.create(UserTable)
        }
        
        repository = UserRepositoryImpl()
    }
    
    @Test
    fun `create inserts new user and returns with generated id`() = runBlocking {
        // Arrange
        val user = User(
            name = "Test User",
            email = "test@example.com",
            active = true,
            createdAt = System.currentTimeMillis()
        )
        
        // Act
        val createdUser = repository.create(user)
        
        // Assert
        assertNotNull(createdUser.id)
        assertEquals("Test User", createdUser.name)
        assertEquals("test@example.com", createdUser.email)
        assertTrue(createdUser.active)
    }
    
    @Test
    fun `findById returns user when exists`() = runBlocking {
        // Arrange
        val user = User(
            name = "Test User",
            email = "test@example.com"
        )
        val createdUser = repository.create(user)
        
        // Act
        val foundUser = repository.findById(createdUser.id!!)
        
        // Assert
        assertNotNull(foundUser)
        assertEquals(createdUser.id, foundUser?.id)
        assertEquals("Test User", foundUser?.name)
    }
    
    @Test
    fun `findById returns null when user not found`() = runBlocking {
        // Act
        val foundUser = repository.findById(999)
        
        // Assert
        assertNull(foundUser)
    }
    
    @Test
    fun `findAll returns all users`() = runBlocking {
        // Arrange
        repository.create(User(name = "User 1", email = "user1@example.com"))
        repository.create(User(name = "User 2", email = "user2@example.com"))
        
        // Act
        val users = repository.findAll()
        
        // Assert
        assertEquals(2, users.size)
    }
    
    @Test
    fun `findByName returns users with matching name`() = runBlocking {
        // Arrange
        repository.create(User(name = "John Doe", email = "john@example.com"))
        repository.create(User(name = "Johnny Smith", email = "johnny@example.com"))
        repository.create(User(name = "Jane Smith", email = "jane@example.com"))
        
        // Act
        val usersWithJohn = repository.findByName("John")
        
        // Assert
        assertEquals(2, usersWithJohn.size)
        assertTrue(usersWithJohn.all { it.name.contains("John") })
    }
    
    @Test
    fun `update modifies existing user`() = runBlocking {
        // Arrange
        val user = User(name = "Original Name", email = "original@example.com")
        val createdUser = repository.create(user)
        val updatedUser = User(
            id = createdUser.id,
            name = "Updated Name",
            email = "updated@example.com",
            active = false,
            createdAt = createdUser.createdAt
        )
        
        // Act
        val result = repository.update(createdUser.id!!, updatedUser)
        
        // Assert
        assertNotNull(result)
        assertEquals(createdUser.id, result?.id)
        assertEquals("Updated Name", result?.name)
        assertEquals("updated@example.com", result?.email)
        assertFalse(result?.active ?: true)
    }
    
    @Test
    fun `update returns null when user doesn't exist`() = runBlocking {
        // Arrange
        val nonExistentUser = User(
            id = 999,
            name = "Non-existent User",
            email = "nonexistent@example.com"
        )
        
        // Act
        val result = repository.update(999, nonExistentUser)
        
        // Assert
        assertNull(result)
    }
    
    @Test
    fun `delete removes user and returns true when successful`() = runBlocking {
        // Arrange
        val user = User(name = "Test User", email = "test@example.com")
        val createdUser = repository.create(user)
        
        // Act
        val deleteResult = repository.delete(createdUser.id!!)
        
        // Assert
        assertTrue(deleteResult)
        assertNull(repository.findById(createdUser.id!!))
    }
    
    @Test
    fun `delete returns false when user doesn't exist`() = runBlocking {
        // Act
        val result = repository.delete(999)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `findAllPaginated returns correct page of results`() = runBlocking {
        // Arrange - Create 15 users
        repeat(15) { i ->
            repository.create(User(name = "User $i", email = "user$i@example.com"))
        }
        
        // Act - Get first page (10 items)
        val page1 = repository.findAllPaginated(1, 10)
        // Get second page (remaining 5 items)
        val page2 = repository.findAllPaginated(2, 10)
        
        // Assert
        assertEquals(10, page1.size)
        assertEquals(5, page2.size)
    }
    
    @Test
    fun `count returns total number of users`() = runBlocking {
        // Arrange - Create 5 users
        repeat(5) { i ->
            repository.create(User(name = "User $i", email = "user$i@example.com"))
        }
        
        // Act
        val count = repository.count()
        
        // Assert
        assertEquals(5, count)
    }
}

