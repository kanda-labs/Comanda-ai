package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus
import kandalabs.commander.domain.service.TableService
import kandalabs.commander.presentation.models.request.CreateTableRequest
import kandalabs.commander.domain.model.ErrorResponse
import kandalabs.commander.domain.model.toResponse
import kandalabs.commander.presentation.models.request.UpdateTableRequest
import kandalabs.commander.domain.service.OrderService
import mu.KotlinLogging
import kotlin.fold

private val logger = KotlinLogging.logger {}

/**
 * Extension function to configure table-related routes
 */
fun Route.tableRoutes(tableService: TableService, orderService: OrderService) {
    route("/tables") {
            // Get all tables
            get {
                try {
                    logger.info { "Fetching all tables" }
                    tableService.getAllTables()
                        .fold(
                            onSuccess = { tables ->
                                call.respond(HttpStatusCode.OK, tables.map {  it.toResponse() })
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error fetching tables" }
                                val errorResponse = ErrorResponse(
                                    status = HttpStatusCode.InternalServerError.value,
                                    message = "Error fetching tables: ${error.message}",
                                    path = call.request.path()
                                )
                                call.respond(HttpStatusCode.InternalServerError, errorResponse)
                            }
                        )
                } catch (e: Exception) {
                    logger.error(e) { "Unexpected error fetching tables" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.InternalServerError.value,
                        message = "Unexpected error: ${e.message}",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.InternalServerError, errorResponse)
                }
            }

            // Get table by ID
            get("{id}") {
                try {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID format")
                    logger.info { "Fetching table with ID: $id" }

                    tableService.getTableById(id.toInt())
                        .fold(
                            onSuccess = { table ->
                                if (table != null) {
                                    // Buscar ordens vinculadas ao billId da mesa, se existir
                                    val orders = table.billId?.let { billId ->
                                        orderService.getOrdersByBillId(billId)
                                    } ?: emptyList()
                                    // Retornar a mesa com as ordens
                                    call.respond(HttpStatusCode.OK, table.copy(orders = orders).toResponse())
                                } else {
                                    val errorResponse = ErrorResponse(
                                        status = HttpStatusCode.NotFound.value,
                                        message = "Table not found with ID: $id",
                                        path = call.request.path()
                                    )
                                    call.respond(HttpStatusCode.NotFound, errorResponse)
                                }
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error fetching table with ID: $id" }
                                val errorResponse = ErrorResponse(
                                    status = HttpStatusCode.InternalServerError.value,
                                    message = "Error fetching table: ${error.message}",
                                    path = call.request.path()
                                )
                                call.respond(HttpStatusCode.InternalServerError, errorResponse)
                            }
                        )
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Validation error in get table by ID: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = e.message ?: "Invalid table ID",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            // Create new table
            post {
                try {
                    val request = call.receive<CreateTableRequest>()
                    val table = Table(
                        id = null,
                        number = request.number,
                        status = TableStatus.CLOSED,
                        createdAt =localDateTimeNow(),
                    )
                    tableService.createTable(table)
                        .fold(
                            onSuccess = { createdTable ->
                                call.respond(HttpStatusCode.Created, createdTable.toResponse())
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error creating table" }
                                val errorResponse = ErrorResponse(
                                    status = HttpStatusCode.InternalServerError.value,
                                    message = "Error creating table: ${error.message}",
                                    path = call.request.path()
                                )
                                call.respond(HttpStatusCode.InternalServerError, errorResponse)
                            }
                        )
                } catch (e: Exception) {
                    logger.error(e) { "Unexpected error creating table" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = "Invalid request: ${e.message}",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            // Update table by ID
            put("{id}") {
                try {
                    val tableId = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID format")
                    val request = runCatching { call.receive<UpdateTableRequest>() }
                        .fold(
                            onSuccess = { it },
                            onFailure = {
                                logger.warn { "Validation error in update table: ${it.message}" }
                                throw IllegalArgumentException("Invalid request format")
                            }
                        )
                    logger.info { "Updating table with ID: $tableId" }

                    tableService.updateTable(tableId, newBillId = request.billId, newStatus = request.status)
                        .fold(
                            onSuccess = { updatedTable ->
                                if (updatedTable != null) {
                                    call.respond(HttpStatusCode.OK, updatedTable.toResponse())
                                } else {
                                    val errorResponse = ErrorResponse(
                                        status = HttpStatusCode.NotFound.value,
                                        message = "Table not found with ID: $tableId",
                                        path = call.request.path()
                                    )
                                    call.respond(HttpStatusCode.NotFound, errorResponse)
                                }
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error updating table with ID: $tableId" }
                                val errorResponse = ErrorResponse(
                                    status = HttpStatusCode.InternalServerError.value,
                                    message = "Error updating table: ${error.message}",
                                    path = call.request.path()
                                )
                                call.respond(HttpStatusCode.InternalServerError, errorResponse)
                            }
                        )
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Validation error in update table: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = e.message ?: "Invalid table ID",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }

            // Delete table by ID
            delete("{id}") {
                try {
                    val id = call.parameters["id"] ?: throw IllegalArgumentException("Invalid ID format")
                    logger.info { "Deleting table with ID: $id" }

                    tableService.deleteTable(id.toInt())
                        .fold(
                            onSuccess = { success ->
                                if (success) {
                                    call.respond(HttpStatusCode.NoContent)
                                } else {
                                    val errorResponse = ErrorResponse(
                                        status = HttpStatusCode.NotFound.value,
                                        message = "Table not found with ID: $id",
                                        path = call.request.path()
                                    )
                                    call.respond(HttpStatusCode.NotFound, errorResponse)
                                }
                            },
                            onFailure = { error ->
                                logger.error(error) { "Error deleting table with ID: $id" }
                                val errorResponse = ErrorResponse(
                                    status = HttpStatusCode.InternalServerError.value,
                                    message = "Error deleting table: ${error.message}",
                                    path = call.request.path()
                                )
                                call.respond(HttpStatusCode.InternalServerError, errorResponse)
                            }
                        )
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Validation error in delete table: ${e.message}" }
                    val errorResponse = ErrorResponse(
                        status = HttpStatusCode.BadRequest.value,
                        message = e.message ?: "Invalid table ID",
                        path = call.request.path()
                    )
                    call.respond(HttpStatusCode.BadRequest, errorResponse)
                }
            }
        }
}
