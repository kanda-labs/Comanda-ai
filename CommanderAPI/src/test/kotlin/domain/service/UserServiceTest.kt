package kandalabs.commander.domain.service

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kandalabs.commander.domain.model.User
import kandalabs.commander.domain.repository.UserRepository
import kotlin.test.*

/**
 * Unit tests for UserService
 * 
 * These tests verify the functionality of the UserService, including:
 * - Basic CRUD operations
 * - Validation of user input
 * - Error handling
 * - Pagination logic
 * - Search functionality
 */
class UserServiceTest {
    // Mock dependencies
    private val mockRepository: UserRepository = mockk()
    
    // System under test
    private lateinit var userService: UserService
    
    @BeforeEach
    fun setup() {
        clearAllMocks()
        userService = UserService(mockRepository)
    }
    
    //---------------------------------------------------------------
    // READ OPERATIONS
    //---------------------------------------------------------------
    
    @Test
    fun `getAllUsers should return list of users from repository`() = runBlocking {
        // Arrange
        val mockUserRespons = listOf(
            User(id = 1, name = "John Doe", email = "john@example.com"),
            User(id = 2, name = "Jane Smith", email = "jane@example.com")
        )
        coEvery { mockRepository.findAll() } returns mockUserRespons
        
        // Act
        val result = userService.getAllUsers()
        
        // Assert
        assertEquals(2, result.size)
        assertEquals("John Doe", result[0].name)
        assertEquals("Jane Smith", result[1].name)
        coVerify(exactly = 1) { mockRepository.findAll() }
    }
    
    @Test
    fun `getUserById should return user when found`() = runBlocking {
        // Arrange
        val userId = 1
        val mockUser = User(id = userId, name = "John Doe", email = "john@example.com")
        coEvery { mockRepository.findById(userId) } returns mockUser
        
        // Act
        val result = userService.getUserById(userId)
        
        // Assert
        assertNotNull(result)
        assertEquals(userId, result?.id)
        assertEquals("John Doe", result?.name)
        coVerify(exactly = 1) { mockRepository.findById(userId) }
    }
    
    @Test
    fun `getUserById should return null when user not found`() = runBlocking {
        // Arrange
        val userId = 999
        coEvery { mockRepository.findById(userId) } returns null
        
        // Act
        val result = userService.getUserById(userId)
        
        // Assert
        assertNull(result)
        coVerify(exactly = 1) { mockRepository.findById(userId) }
    }
    
    @Test
    fun `searchUsersByName should return matching users`() = runBlocking {
        // Arrange
        val searchName = "John"
        val mockUserRespons = listOf(
            User(id = 1, name = "John Doe", email = "john@example.com"),
            User(id = 2, name = "Johnny Smith", email = "johnny@example.com")
        )
        coEvery { mockRepository.findByName(searchName) } returns mockUserRespons
        
        // Act
        val result = userService.searchUsersByName(searchName)
        
        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains(searchName, ignoreCase = true) })
        coVerify(exactly = 1) { mockRepository.findByName(searchName) }
    }
    
    //---------------------------------------------------------------
    // CREATE OPERATIONS
    //---------------------------------------------------------------
    
    @Test
    fun `createUser should return success when input is valid`() = runBlocking {
        // Arrange
        val newUser = User(
            name = "John Doe",
            email = "john@example.com",
            active = true
        )
        val createdUser = newUser.copy(id = 1)
        coEvery { mockRepository.create(any()) } returns createdUser
        
        // Act
        val result = userService.createUser(newUser)
        
        // Assert
        assertTrue(result.isSuccess)
        result.onSuccess { user ->
            assertNotNull(user.id)
            assertEquals(1, user.id)
            assertEquals("John Doe", user.name)
            assertEquals("john@example.com", user.email)
        }
        coVerify(exactly = 1) { mockRepository.create(any()) }
    }
    
    @Test
    fun `createUser should fail when name is blank`() = runBlocking {
        // Arrange
        val invalidUser = User(
            name = "", // Invalid blank name
            email = "john@example.com"
        )
        
        // Act
        val result = userService.createUser(invalidUser)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception.message?.contains("cannot be blank") == true)
        }
        coVerify(exactly = 0) { mockRepository.create(any()) }
    }
    
    @Test
    fun `createUser should fail when email is invalid`() = runBlocking {
        // Arrange
        val invalidUser = User(
            name = "John Doe",
            email = "invalid-email" // Invalid email format
        )
        
        // Act
        val result = userService.createUser(invalidUser)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception.message?.contains("Invalid email format") == true)
        }
        coVerify(exactly = 0) { mockRepository.create(any()) }
    }
    
    @Test
    fun `createUser should handle repository exceptions`() = runBlocking {
        // Arrange
        val validUser = User(
            name = "John Doe",
            email = "john@example.com"
        )
        coEvery { mockRepository.create(any()) } throws RuntimeException("Database error")
        
        // Act
        val result = userService.createUser(validUser)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is RuntimeException)
            assertEquals("Database error", exception.message)
        }
        coVerify(exactly = 1) { mockRepository.create(any()) }
    }
    
    //---------------------------------------------------------------
    // UPDATE OPERATIONS
    //---------------------------------------------------------------
    
    @Test
    fun `updateUser should return success when user exists and data is valid`() = runBlocking {
        // Arrange
        val userId = 1
        val updateUser = User(
            id = userId,
            name = "John Updated",
            email = "john.updated@example.com"
        )
        coEvery { mockRepository.update(eq(userId), any()) } returns updateUser
        
        // Act
        val result = userService.updateUser(userId, updateUser)
        
        // Assert
        assertTrue(result.isSuccess)
        result.onSuccess { user ->
            assertNotNull(user)
            assertEquals("John Updated", user?.name)
            assertEquals("john.updated@example.com", user?.email)
        }
        coVerify(exactly = 1) { mockRepository.update(eq(userId), any()) }
    }
    
    @Test
    fun `updateUser should fail when user does not exist`() = runBlocking {
        // Arrange
        val userId = 999
        val updateUser = User(
            id = userId,
            name = "John Doe",
            email = "john@example.com"
        )
        coEvery { mockRepository.update(eq(userId), any()) } returns null
        
        // Act
        val result = userService.updateUser(userId, updateUser)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is NoSuchElementException)
            assertTrue(exception.message?.contains("not found") == true)
        }
        coVerify(exactly = 1) { mockRepository.update(eq(userId), any()) }
    }
    
    @Test
    fun `updateUser should validate input data`() = runBlocking {
        // Arrange
        val userId = 1
        val invalidUser = User(
            id = userId,
            name = "", // Invalid blank name
            email = "john@example.com"
        )
        
        // Act
        val result = userService.updateUser(userId, invalidUser)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception.message?.contains("cannot be blank") == true)
        }
        coVerify(exactly = 0) { mockRepository.update(any(), any()) }
    }
    
    //---------------------------------------------------------------
    // DELETE OPERATIONS
    //---------------------------------------------------------------
    
    @Test
    fun `deleteUser should return success when user exists`() = runBlocking {
        // Arrange
        val userId = 1
        coEvery { mockRepository.delete(userId) } returns true
        
        // Act
        val result = userService.deleteUser(userId)
        
        // Assert
        assertTrue(result.isSuccess)
        result.onSuccess { deleted ->
            assertTrue(deleted)
        }
        coVerify(exactly = 1) { mockRepository.delete(userId) }
    }
    
    @Test
    fun `deleteUser should fail when user does not exist`() = runBlocking {
        // Arrange
        val userId = 999
        coEvery { mockRepository.delete(userId) } returns false
        
        // Act
        val result = userService.deleteUser(userId)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is NoSuchElementException)
            assertTrue(exception.message?.contains("not found") == true)
        }
        coVerify(exactly = 1) { mockRepository.delete(userId) }
    }
    
    //---------------------------------------------------------------
    // PAGINATION OPERATIONS
    //---------------------------------------------------------------
    
    @Test
    fun `getPaginatedUsers should return success with users and count`() = runBlocking {
        // Arrange
        val page = 2
        val size = 10
        val userRespons = listOf(
            User(id = 11, name = "User 11"),
            User(id = 12, name = "User 12")
        )
        val totalCount = 25L
        
        coEvery { mockRepository.findAllPaginated(page, size) } returns userRespons
        coEvery { mockRepository.count() } returns totalCount
        
        // Act
        val result = userService.getPaginatedUsers(page, size)
        
        // Assert
        assertTrue(result.isSuccess)
        result.onSuccess { (resultUsers, count) ->
            assertEquals(2, resultUsers.size)
            assertEquals(25L, count)
            assertEquals("User 11", resultUsers[0].name)
        }
        coVerify(exactly = 1) { 
            mockRepository.findAllPaginated(page, size)
            mockRepository.count() 
        }
    }
    
    @Test
    fun `getPaginatedUsers should normalize invalid page and size values`() = runBlocking {
        // Arrange
        val invalidPage = -1
        val invalidSize = 0
        
        coEvery { mockRepository.findAllPaginated(1, 10) } returns emptyList()
        coEvery { mockRepository.count() } returns 0
        
        // Act
        val result = userService.getPaginatedUsers(invalidPage, invalidSize)
        
        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockRepository.findAllPaginated(1, 10) }
    }
    
    @Test
    fun `getPaginatedUsers should cap size to maximum allowed`() = runBlocking {
        // Arrange
        val page = 1
        val tooLargeSize = 500
        val maxAllowedSize = 100 // Assuming this is the max allowed size
        
        coEvery { mockRepository.findAllPaginated(page, maxAllowedSize) } returns emptyList()
        coEvery { mockRepository.count() } returns 0
        
        // Act
        val result = userService.getPaginatedUsers(page, tooLargeSize)
        
        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockRepository.findAllPaginated(page, maxAllowedSize) }
    }
    
    //---------------------------------------------------------------
    // EMAIL VALIDATION EDGE CASES
    //---------------------------------------------------------------
    
    @Test
    fun `createUser should accept null email`() = runBlocking {
        // Arrange
        val userWithNullEmail = User(
            name = "John Doe",
            email = null,
            active = true
        )
        val createdUser = userWithNullEmail.copy(id = 1)
        coEvery { mockRepository.create(any()) } returns createdUser
        
        // Act
        val result = userService.createUser(userWithNullEmail)
        
        // Assert
        assertTrue(result.isSuccess)
        result.onSuccess { user ->
            assertEquals(1, user.id)
            assertNull(user.email)
        }
        coVerify(exactly = 1) { mockRepository.create(any()) }
    }
    
    @Test
    fun `updateUser should validate complex email formats`() = runBlocking {
        // Arrange
        val userId = 1
        val validEmails = listOf(
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-hyphen@example.com",
            "fully-qualified-domain@example.com",
            "user.name+tag+sorting@example.com",
            "x@example.com"
        )
        
        val invalidEmails = listOf(
            "plainaddress",
            "#@%^%#$@#$@#.com",
            "@example.com",
            "email.example.com",
            "email@example@example.com",
            ".email@example.com",
            "email.@example.com",
            "email..email@example.com",
            "email@example..com"
        )
        
        // Test valid emails (should not fail validation)
        validEmails.forEach { email ->
            val user = User(id = userId, name = "Test User", email = email)
            coEvery { mockRepository.update(userId, user) } returns user
            
            val result = userService.updateUser(userId, user)
            assertTrue(result.isSuccess, "Email '$email' should be valid")
        }
        
        // Test invalid emails (should fail validation)
        invalidEmails.forEach { email ->
            val user = User(id = userId, name = "Test User", email = email)
            
            val result = userService.updateUser(userId, user)
            assertTrue(result.isFailure, "Email '$email' should be invalid")
            result.onFailure { exception ->
                assertTrue(exception is IllegalArgumentException)
                assertTrue(exception.message?.contains("Invalid email format") == true)
            }
        }
    }
    
    //---------------------------------------------------------------
    // REPOSITORY ERROR HANDLING
    //---------------------------------------------------------------
    
    @Test
    fun `getUserById should handle repository exceptions`() = runBlocking {
        // Arrange
        val userId = 1
        coEvery { mockRepository.findById(userId) } throws RuntimeException("Database error")
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            userService.getUserById(userId)
        }
        assertEquals("Database error", exception.message)
    }
    
    @Test
    fun `findAllPaginated should handle repository exceptions`(): Unit = runBlocking {
        // Arrange
        coEvery { mockRepository.findAllPaginated(any(), any()) } throws RuntimeException("Database error")
        coEvery { mockRepository.count() } returns 0L
        
        // Act
        val result = userService.getPaginatedUsers(1, 10)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is RuntimeException)
            assertEquals("Database error", exception.message)
        }
    }
    
    //---------------------------------------------------------------
    // PAGINATION EDGE CASES 
    //---------------------------------------------------------------
    
    @Test
    fun `getPaginatedUsers should handle empty result sets`(): Unit = runBlocking {
        // Arrange
        coEvery { mockRepository.findAllPaginated(any(), any()) } returns emptyList()
        coEvery { mockRepository.count() } returns 0L
        
        // Act
        val result = userService.getPaginatedUsers(1, 10)
        
        // Assert
        assertTrue(result.isSuccess)
        result.onSuccess { (users, count) ->
            assertTrue(users.isEmpty())
            assertEquals(0L, count)
        }
    }
    
    @Test
    fun `getPaginatedUsers should limit size to reasonable values`() = runBlocking {
        // Arrange
        val extremeValues = listOf(
            Pair(1, Int.MAX_VALUE),   // Maximum possible size
            Pair(Int.MAX_VALUE, 10),  // Maximum possible page
            Pair(0, 0),               // Both zero
            Pair(-1, -10)             // Negative values
        )
        
        extremeValues.forEach { (page, size) ->
            // Setup expectations
            val normalizedPage = if (page < 1) 1 else page
            val normalizedSize = when {
                size < 1 -> 10
                size > 100 -> 100
                else -> size
            }
            
            coEvery { mockRepository.findAllPaginated(normalizedPage, normalizedSize) } returns emptyList()
            coEvery { mockRepository.count() } returns 0L
            
            // Act
            val result = userService.getPaginatedUsers(page, size)
            
            // Assert
            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { mockRepository.findAllPaginated(normalizedPage, normalizedSize) }
            
            // Clear verification
            clearMocks(mockRepository, verificationMarks = true)
        }
    }
    
    //---------------------------------------------------------------
    // SEARCH EDGE CASES
    //---------------------------------------------------------------
    
    @Test
    fun `searchUsersByName should handle empty search term`() = runBlocking {
        // Arrange
        val emptySearchTerm = ""
        coEvery { mockRepository.findByName(emptySearchTerm) } returns emptyList()
        
        // Act
        val result = userService.searchUsersByName(emptySearchTerm)
        
        // Assert
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { mockRepository.findByName(emptySearchTerm) }
    }
    
    @Test
    fun `searchUsersByName should handle special characters`() = runBlocking {
        // Arrange
        val specialChars = "!@#$%^&*()_+{}[]|:;<>,.?/"
        coEvery { mockRepository.findByName(specialChars) } returns emptyList()
        
        // Act
        val result = userService.searchUsersByName(specialChars)
        
        // Assert
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { mockRepository.findByName(specialChars) }
    }
    
    @Test
    fun `searchUsersByName should handle very long search terms`() = runBlocking {
        // Arrange
        val veryLongName = "a".repeat(1000)
        coEvery { mockRepository.findByName(veryLongName) } returns emptyList()
        
        // Act
        val result = userService.searchUsersByName(veryLongName)
        
        // Assert
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { mockRepository.findByName(veryLongName) }
    }
    
    //---------------------------------------------------------------
    // EMPTY RESULT HANDLING
    //---------------------------------------------------------------
    
    @Test
    fun `findAll should handle empty result sets`() = runBlocking {
        // Arrange
        coEvery { mockRepository.findAll() } returns emptyList()
        
        // Act
        val result = userService.getAllUsers()
        
        // Assert
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `deleteUser should handle non-existent users gracefully`():Unit = runBlocking {
        // Arrange
        val nonExistentId = -1
        coEvery { mockRepository.delete(nonExistentId) } returns false
        
        // Act
        val result = userService.deleteUser(nonExistentId)
        
        // Assert
        assertTrue(result.isFailure)
        result.onFailure { exception ->
            assertTrue(exception is NoSuchElementException)
        }
    }
}
