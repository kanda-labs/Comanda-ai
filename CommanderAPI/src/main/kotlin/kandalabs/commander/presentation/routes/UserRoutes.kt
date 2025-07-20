package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kandalabs.commander.data.repository.toLocalDateTime
import kandalabs.commander.domain.model.User
import kandalabs.commander.domain.service.UserService
import kandalabs.commander.presentation.models.request.CreateUserRequest
import kandalabs.commander.presentation.models.request.PaginationRequest
import kandalabs.commander.presentation.models.request.UpdateUserRequest
import kandalabs.commander.domain.model.ErrorResponse
import kandalabs.commander.domain.model.PaginatedResponse
import kandalabs.commander.domain.model.toResponse
import mu.KotlinLogging
import kotlin.math.ceil

private val logger = KotlinLogging.logger {}

/**
 * Validates user request data
 */
private fun validateUserRequest(name: String, email: String?) {
    if (name.isBlank()) {
        throw IllegalArgumentException("Name cannot be empty")
    }
    if (email != null && email.isBlank()) {
        throw IllegalArgumentException("Email cannot be empty")
    } else if (email?.matches(Regex(".+@.+\\..+")) == false) {
        throw IllegalArgumentException("Invalid email format")
    }
}

/**
 * Extension function to configure user-related routes
 */
fun Route.userRoutes(userService: UserService) {
    route("/users") {
            // Get all users with pagination
            get {
                try {
                    // Extract pagination parameters from query parameters
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 10

                    if (page < 1 || size < 1) {
                        throw IllegalArgumentException("Page and size must be positive numbers")
                    }

                    val paginationRequest = PaginationRequest(page, size)
                    logger.info { "Getting users with pagination: $paginationRequest" }

                    userService.getPaginatedUsers(paginationRequest.page, paginationRequest.size)
                        .fold(
                            onSuccess = { (users, total) ->
                                val totalPages = ceil(total.toDouble() / paginationRequest.size).toInt()

                                val response = PaginatedResponse(
                                    items = users,
                                    total = total,
                                    page = paginationRequest.page,
                                    size = paginationRequest.size,
                                    totalPages = totalPages,
                                    hasNext = paginationRequest.page < totalPages,
                                    hasPrevious = paginationRequest.page > 1
                                )

                                call.respond(HttpStatusCode.OK, response)
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error fetching paginated users" }
                                val errorResponse = ErrorResponse(
                                    status = HttpStatusCode.InternalServerError.value,
                                    message = "Error fetching users: ${error.message}",
                                    path = call.request.path()
                                )
                                call.respond(HttpStatusCode.InternalServerError, errorResponse)
                            }
                        )
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Validation error in get users: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = e.message ?: "Invalid pagination parameters",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            // Get user by ID
            get("{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid ID format")

                    logger.info { "Getting user by id: $id" }

                    val user = userService.getUserById(id)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        val errorResponse = ErrorResponse(
                            status = HttpStatusCode.NotFound.value,
                            message = "User not found with ID: $id",
                            path = call.request.path()
                        )
                        call.respond(HttpStatusCode.NotFound, errorResponse)
                    }
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Validation error in get user by id: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = e.message ?: "Invalid user ID",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            post {
                try {
                    val request = call.receive<CreateUserRequest>()

                    validateUserRequest(request.name, request.email)

                    logger.info { "Creating user with name: ${request.name}" }

                    val user = User(
                        id = null,
                        name = request.name,
                        email = request.email,
                        active = request.active,
                        createdAt = System.currentTimeMillis().toLocalDateTime(),
                    )

                    userService.createUser(user)
                        .fold(
                            onSuccess = { createdUser ->
                                call.respond(HttpStatusCode.Created, createdUser)
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error creating user" }
                                when (error) {
                                    is IllegalArgumentException -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.BadRequest.value,
                                            message = error.message ?: "Invalid user data",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                                    }
                                    else -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.InternalServerError.value,
                                            message = "Error creating user: ${error.message}",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.InternalServerError, errorResponse)
                                    }
                                }
                            }
                        )
                } catch (e: Exception) {
                    logger.error(e) { "Unexpected error in create user" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = "Invalid request: ${e.message}",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            // Update existing user
            put("{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid ID format")

                    val request = call.receive<UpdateUserRequest>()

                    // Validate request
                    validateUserRequest(request.name, request.email)

                    logger.info { "Updating user with id: $id" }

                    // Check if user exists
                    val existingUser = userService.getUserById(id)
                        ?: throw NoSuchElementException("User not found with ID: $id")

                    // Map request DTO to domain entity, preserving creation timestamp
                    val user = User(
                        id = id,
                        name = request.name,
                        email = request.email,
                        active = request.active ?: existingUser.active,
                        createdAt = existingUser.createdAt
                    )

                    userService.updateUser(id, user)
                        .fold(
                            onSuccess = { updatedUser ->
                                if (updatedUser != null) {
                                    call.respond(HttpStatusCode.OK, updatedUser)
                                } else {
                                    val errorResponse = ErrorResponse(
                                        status = HttpStatusCode.NotFound.value,
                                        message = "User not found with ID: $id",
                                        path = call.request.path()
                                    )
                                    call.respond(HttpStatusCode.NotFound, errorResponse)
                                }
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error updating user" }
                                when (error) {
                                    is NoSuchElementException -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.NotFound.value,
                                            message = error.message ?: "User not found",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.NotFound, errorResponse)
                                    }
                                    is IllegalArgumentException -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.BadRequest.value,
                                            message = error.message ?: "Invalid user data",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.BadRequest, errorResponse)
                                    }
                                    else -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.InternalServerError.value,
                                            message = "Error updating user: ${error.message}",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.InternalServerError, errorResponse)
                                    }
                                }
                            }
                        )
                } catch (e: NoSuchElementException) {
                    logger.warn { "User not found in update: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.NotFound.value,
                        message = e.message ?: "User not found",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.NotFound, errorResponse)
                } catch (e: Exception) {
                    logger.error(e) { "Unexpected error in update user" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = "Invalid request: ${e.message}",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            // Delete user
            delete("{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid ID format")

                    logger.info { "Deleting user with id: $id" }

                    userService.deleteUser(id)
                        .fold(
                            onSuccess = { success ->
                                if (success) {
                                    call.respond(HttpStatusCode.NoContent)
                                } else {
                                    val errorResponse = ErrorResponse(
                                        status = HttpStatusCode.NotFound.value,
                                        message = "User not found with ID: $id",
                                        path = call.request.path()
                                    )
                                    call.respond(HttpStatusCode.NotFound, errorResponse)
                                }
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error deleting user" }
                                when (error) {
                                    is NoSuchElementException -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.NotFound.value,
                                            message = error.message ?: "User not found",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.NotFound, errorResponse)
                                    }
                                    else -> {
                                        val errorResponse = ErrorResponse(
                                            status = HttpStatusCode.InternalServerError.value,
                                            message = "Error deleting user: ${error.message}",
                                            path = call.request.path()
                                        )
                                        call.respond(HttpStatusCode.InternalServerError, errorResponse)
                                    }
                                }
                            }
                        )
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Validation error in delete user: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = e.message ?: "Invalid user ID",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }
        }
}