package kandalabs.commander.presentation.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

import kandalabs.commander.domain.model.User
import kandalabs.commander.domain.service.UserService
import kandalabs.commander.infrastructure.framework.ktor.configurePlugins
import kandalabs.commander.presentation.models.request.CreateUserRequest
import kandalabs.commander.presentation.models.request.UpdateUserRequest
import kandalabs.commander.domain.model.ErrorResponse

class UserRoutesTest {
    private val mockUserService = mockk<UserService>()
    private val json = Json { ignoreUnknownKeys = true }
    
    // Test helper for creating LocalDateTime
    private fun testDateTime(): LocalDateTime = 
        Instant.fromEpochMilliseconds(System.currentTimeMillis())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    
    @BeforeEach
    fun setup() {
        clearAllMocks()
    }
    
    private fun Application.testModule() {
        configurePlugins()
        routing {
            userRoutes(mockUserService)
        }
    }
    
    @Test
    fun `GET users returns 200 with paginated list`() = testApplication {
        // Arrange
        val mockUserRespons = listOf(
            User(id = 1, name = "John Doe", email = "john@example.com", createdAt = testDateTime()),
            User(id = 2, name = "Jane Smith", email = "jane@example.com", createdAt = testDateTime())
        )
        coEvery { mockUserService.getPaginatedUsers(1, 10) } returns Result.success(Pair(mockUserRespons, 2L))
        
        application {
            testModule()
        }
        
        // Act
        val response = client.get("/api/v1/users?page=1&size=10")
        
        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val responseBody = json.decodeFromString<Map<String, JsonElement>>(response.bodyAsText())
        assertEquals(2, responseBody["total"]?.jsonPrimitive?.longOrNull)
        assertEquals(1, responseBody["page"]?.jsonPrimitive?.intOrNull)
        assertEquals(10, responseBody["size"]?.jsonPrimitive?.intOrNull)
        
        val items = responseBody["items"]?.jsonArray
        assertEquals(2, items?.size)
        
        coVerify(exactly = 1) { mockUserService.getPaginatedUsers(1, 10) }
    }
    
    @Test
    fun `GET users returns 400 with invalid parameters`() = testApplication {
        // Arrange
        application {
            testModule()
        }
        
        // Act
        val response = client.get("/api/v1/users?page=invalid&size=10")
        
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        val responseBody = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertTrue(responseBody.message.contains("Invalid pagination parameters"))
    }
    
    @Test
    fun `GET user by id returns 200 with user when found`() = testApplication {
        // Arrange
        val userId = 1
        val mockUser = User(id = userId, name = "John Doe", email = "john@example.com", createdAt = testDateTime())
        coEvery { mockUserService.getUserById(userId) } returns mockUser
        
        application {
            testModule()
        }
        
        // Act
        val response = client.get("/api/v1/users/$userId")
        
        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val user = json.decodeFromString<User>(response.bodyAsText())
        assertEquals(userId, user.id)
        assertEquals("John Doe", user.name)
        
        coVerify(exactly = 1) { mockUserService.getUserById(userId) }
    }
    
    @Test
    fun `GET user by id returns 404 when user not found`() = testApplication {
        // Arrange
        val userId = 999
        coEvery { mockUserService.getUserById(userId) } returns null
        
        application {
            testModule()
        }
        
        // Act
        val response = client.get("/api/v1/users/$userId")
        
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertTrue(errorResponse.message.contains("User not found"))
        
        coVerify(exactly = 1) { mockUserService.getUserById(userId) }
    }
    
    @Test
    fun `POST users returns 201 when user created successfully`() = testApplication {
        // Arrange
        val createRequest = CreateUserRequest(
            name = "John Doe",
            email = "john@example.com",
            active = true
        )
        
        val createdUser = User(
            id = 1,
            name = createRequest.name,
            email = createRequest.email,
            active = createRequest.active,
            createdAt = testDateTime()
        )
        
        coEvery { mockUserService.createUser(any()) } returns Result.success(createdUser)
        
        application {
            testModule()
        }
        
        // Act
        val response = client.post("/api/v1/users") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(createRequest))
        }
        
        // Assert
        assertEquals(HttpStatusCode.Created, response.status)
        
        val user = json.decodeFromString<User>(response.bodyAsText())
        assertEquals(1, user.id)
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
        
        coVerify(exactly = 1) { mockUserService.createUser(any()) }
    }
    
    @Test
    fun `POST users returns 400 when validation fails`() = testApplication {
        // Arrange
        val invalidRequest = CreateUserRequest(
            name = "",  // Invalid: empty name
            email = "invalid-email"  // Invalid: incorrect email format
        )
        
        coEvery { mockUserService.createUser(any()) } returns Result.failure(
            IllegalArgumentException("User name cannot be blank")
        )
        
        application {
            testModule()
        }
        
        // Act
        val response = client.post("/api/v1/users") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(invalidRequest))
        }
        
        // Assert
        assertEquals(HttpStatusCode.BadRequest, response.status)
        
        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertTrue(errorResponse.message.contains("Invalid request"))
    }
    
    @Test
    fun `PUT users returns 200 when user updated successfully`() = testApplication {
        // Arrange
        val userId = 1
        val updateRequest = UpdateUserRequest(
            name = "John Updated",
            email = "john.updated@example.com",
            active = true
        )
        
        val existingUser = User(
            id = userId,
            name = "John Doe",
            email = "john@example.com",
            active = true,
            createdAt = testDateTime()
        )
        
        val updatedUser = existingUser.copy(
            name = updateRequest.name,
            email = updateRequest.email
        )
        
        coEvery { mockUserService.getUserById(userId) } returns existingUser
        coEvery { mockUserService.updateUser(eq(userId), any()) } returns Result.success(updatedUser)
        
        application {
            testModule()
        }
        
        // Act
        val response = client.put("/api/v1/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(updateRequest))
        }
        
        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        
        val user = json.decodeFromString<User>(response.bodyAsText())
        assertEquals(userId, user.id)
        assertEquals("John Updated", user.name)
        assertEquals("john.updated@example.com", user.email)
        
        coVerify(exactly = 1) { mockUserService.updateUser(eq(userId), any()) }
    }
    
    @Test
    fun `PUT users returns 404 when user not found`() = testApplication {
        // Arrange
        val userId = 999
        val updateRequest = UpdateUserRequest(
            name = "John Updated",
            email = "john.updated@example.com"
        )
        
        coEvery { mockUserService.getUserById(userId) } returns null
        
        application {
            testModule()
        }
        
        // Act
        val response = client.put("/api/v1/users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(updateRequest))
        }
        
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertTrue(errorResponse.message.contains("User not found"))
    }
    
    @Test
    fun `DELETE users returns 204 when user deleted successfully`() = testApplication {
        // Arrange
        val userId = 1
        coEvery { mockUserService.deleteUser(userId) } returns Result.success(true)
        
        application {
            testModule()
        }
        
        // Act
        val response = client.delete("/api/v1/users/$userId")
        
        // Assert
        assertEquals(HttpStatusCode.NoContent, response.status)
        coVerify(exactly = 1) { mockUserService.deleteUser(userId) }
    }
    
    @Test
    fun `DELETE users returns 404 when user not found`() = testApplication {
        // Arrange
        val userId = 999
        coEvery { mockUserService.deleteUser(userId) } returns Result.failure(
            NoSuchElementException("User not found with ID: $userId")
        )
        
        application {
            testModule()
        }
        
        // Act
        val response = client.delete("/api/v1/users/$userId")
        
        // Assert
        assertEquals(HttpStatusCode.NotFound, response.status)
        
        val errorResponse = json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertTrue(errorResponse.message.contains("User not found"))
    }
}

