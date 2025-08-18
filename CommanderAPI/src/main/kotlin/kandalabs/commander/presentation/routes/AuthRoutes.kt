package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kandalabs.commander.domain.model.ErrorResponse
import kandalabs.commander.domain.service.UserService
import kandalabs.commander.infrastructure.framework.ktor.JwtConfig
import kotlinx.serialization.Serializable
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val id: Int,
    val name: String,
    val email: String?,
    val role: String,
    val token: String
)

/**
 * Extension function to configure auth-related routes
 */
fun Route.authRoutes(userService: UserService) {
    route("/auth") {
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                
                // Validate request
                if (request.username.isBlank() || request.password.isBlank()) {
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = "Username and password are required",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                    return@post
                }
                
                logger.info { "Login attempt for username: ${request.username}" }
                
                // For demo purposes, we'll create a simple authentication
                // In production, you would validate against database with hashed passwords
                val users = userService.getAllUsers()
                val user = users.find { it.userName == request.username }
                
                // Demo password validation - in production use proper password hashing
                val validPassword = when (user?.userName) {
                    "admin" -> request.password == "122825"
                    "test" -> request.password == "1234"
                    "lueny-paixao" -> request.password == "142824"
                    "leonardo-paixao" -> request.password == "123456"
                    "rennan-viana" -> request.password == "123456"
                    else -> false
                }
                
                if (user != null && validPassword) {
                    val token = JwtConfig.generateToken(user)
                    
                    val response = LoginResponse(
                        id = user.id ?: 0,
                        name = user.name,
                        email = user.email,
                        role = user.role.name,
                        token = token
                    )
                    
                    logger.info { "Login successful for user: ${user.name}" }
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    logger.warn { "Login failed for username: ${request.username}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.Unauthorized.value,
                        message = "Invalid credentials",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.Unauthorized, errorResponse)
                }
                
            } catch (e: Exception) {
                logger.error(e) { "Error in login endpoint" }
                val errorResponse = ErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    message = "Internal server error: ${e.message}",
                    path = call.request.path()
                )
                call.respond(HttpStatusCode.InternalServerError, errorResponse)
            }
        }
    }
}