package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kandalabs.commander.domain.enums.UserRole
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.service.KitchenService
import kandalabs.commander.presentation.models.request.UpdateItemStatusRequest
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import java.util.concurrent.ConcurrentHashMap

fun Route.kitchenRoutes(kitchenService: KitchenService) {
    route("/kitchen") {
        // TODO: Re-enable authentication once auth configuration is properly set up
        // authenticate {
        //     // Interceptor para verificar role KITCHEN ou ADMIN
        //     intercept(ApplicationCallPipeline.Call) {
        //         val userRole = call.principal<UserPrincipal>()?.role
        //         if (userRole != UserRole.KITCHEN && userRole != UserRole.ADMIN) {
        //             call.respond(HttpStatusCode.Forbidden, "Access denied: Kitchen role required")
        //             return@intercept finish()
        //         }
        //     }
        // }
            
        get("/orders") {
            
            kitchenService.getActiveOrdersForKitchen()
                .onSuccess { orders ->
                    call.respond(HttpStatusCode.OK, orders)
                }
                .onFailure { error ->
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to error.message))
                }
        }
        
        put("/orders/{orderId}/items/{itemId}/unit/{unitIndex}") {
            val orderId = call.parameters["orderId"]?.toIntOrNull() 
                ?: throw BadRequestException("Invalid orderId")
            val itemId = call.parameters["itemId"]?.toIntOrNull() 
                ?: throw BadRequestException("Invalid itemId")
            val unitIndex = call.parameters["unitIndex"]?.toIntOrNull() 
                ?: throw BadRequestException("Invalid unitIndex")
            
            val request = call.receive<UpdateItemStatusRequest>()
            
            try {
                val status = ItemStatus.valueOf(request.status)
                
                kitchenService.updateItemUnitStatus(
                    orderId = orderId,
                    itemId = itemId,
                    unitIndex = unitIndex,
                    status = status,
                    updatedBy = "kitchen-user" // TODO: Get from auth when re-enabled
                ).onSuccess {
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                }.onFailure { error ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid status: ${request.status}"))
            }
        }
        
        put("/orders/{orderId}/deliver") {
            val orderId = call.parameters["orderId"]?.toIntOrNull() 
                ?: throw BadRequestException("Invalid orderId")
            
            kitchenService.markOrderAsDelivered(
                orderId = orderId,
                updatedBy = "kitchen-user" // TODO: Get from auth when re-enabled
            ).onSuccess {
                call.respond(HttpStatusCode.OK, mapOf("success" to true))
            }.onFailure { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        }
        
        put("/orders/{orderId}/items/{itemId}/deliver") {
            val orderId = call.parameters["orderId"]?.toIntOrNull() 
                ?: throw BadRequestException("Invalid orderId")
            val itemId = call.parameters["itemId"]?.toIntOrNull() 
                ?: throw BadRequestException("Invalid itemId")
            
            kitchenService.markItemAsDelivered(
                orderId = orderId,
                itemId = itemId,
                updatedBy = "kitchen-user" // TODO: Get from auth when re-enabled
            ).onSuccess {
                call.respond(HttpStatusCode.OK, mapOf("success" to true))
            }.onFailure { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        }
        
        get("/orders/{orderId}/items/{itemId}/statuses") {
            val orderId = call.parameters["orderId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid orderId")
            val itemId = call.parameters["itemId"]?.toIntOrNull()
                ?: throw BadRequestException("Invalid itemId")
            
            
            kitchenService.getItemStatusBreakdown(orderId)
                .onSuccess { statuses ->
                    call.respond(HttpStatusCode.OK, statuses)
                }
                .onFailure { error ->
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to error.message))
                }
        }
        
        // SSE endpoint for kitchen real-time updates
        sse("/events") {
            val logger = KotlinLogging.logger {}
            val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
            val connectionId = "kitchen-${java.time.Instant.now().toEpochMilli()}-${(0..1000).random()}"
            
            logger.info { "New Kitchen SSE connection established: $connectionId" }
            
            try {
                // Send initial connection event
                send(
                    data = json.encodeToString(mapOf("type" to "connected", "connectionId" to connectionId)),
                    event = "connection"
                )
                
                // Create a job for this connection
                val job = launch {
                    while (isActive) {
                        try {
                            // Fetch orders for kitchen ordered by creation time (oldest first)
                            kitchenService.getActiveOrdersForKitchen()
                                .onSuccess { orders ->
                                    // Sort orders by creation time (oldest first)
                                    val orderedOrders = orders.sortedBy { it.createdAt }
                                    
                                    // Send kitchen orders update as JSON string
                                    val event = KitchenOrdersUpdateEvent(
                                        type = "kitchen_orders_update",
                                        orders = orderedOrders,
                                        timestamp = java.time.Instant.now().toEpochMilli()
                                    )
                                    send(
                                        data = json.encodeToString(event),
                                        event = "kitchen_orders"
                                    )
                                }
                                .onFailure { error ->
                                    logger.error(error) { "Error fetching kitchen orders for SSE" }
                                    send(
                                        data = json.encodeToString(mapOf(
                                            "type" to "error",
                                            "message" to "Failed to fetch kitchen orders: ${error.message}"
                                        )),
                                        event = "error"
                                    )
                                }
                            
                            // Send heartbeat
                            send(
                                data = json.encodeToString(mapOf("type" to "heartbeat")),
                                event = "heartbeat"
                            )
                            
                            // Update every 3 seconds for kitchen
                            delay(3000)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            logger.error(e) { "Error sending Kitchen SSE update" }
                            try {
                                send(
                                    data = json.encodeToString(mapOf(
                                        "type" to "error",
                                        "message" to e.message
                                    )),
                                    event = "error"
                                )
                            } catch (_: Exception) {
                                // Connection might be closed
                            }
                        }
                    }
                }
                
                job.join()
                
            } catch (e: CancellationException) {
                logger.info { "Kitchen SSE connection cancelled for $connectionId" }
            } catch (e: Exception) {
                logger.error(e) { "Error in Kitchen SSE connection" }
            } finally {
                logger.info { "Kitchen SSE connection closed for $connectionId" }
            }
        }
        
        // Endpoint to trigger manual update for kitchen
        post("/events/trigger") {
            call.respond(HttpStatusCode.OK, mapOf(
                "message" to "Kitchen update triggered"
            ))
        }
    }
}

@kotlinx.serialization.Serializable
data class KitchenOrdersUpdateEvent(
    val type: String,
    val orders: List<kandalabs.commander.domain.model.KitchenOrder>,
    val timestamp: Long
)

// Helper class for user principal (assuming this exists in your auth system)
data class UserPrincipal(
    val userName: String,
    val role: UserRole
)