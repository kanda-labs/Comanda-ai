package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kandalabs.commander.domain.model.OrderResponse
import kandalabs.commander.domain.model.OrderStatus
import kandalabs.commander.domain.service.OrderService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

fun Route.orderSSERoutes(orderService: OrderService) {
    val logger = KotlinLogging.logger {}
    val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    
    // Store active connections
    val connections = ConcurrentHashMap<String, Job>()
    
    sse("/orders/events") {
        logger.info { "New SSE connection established" }
        val connectionId = call.request.queryParameters["clientId"] ?: generateConnectionId()
        
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
                        // Fetch all orders
                        val orders = orderService.getAllOrders()
                        
                        // Filter orders based on query parameters
                        val statusFilter = call.request.queryParameters["status"]
                        val filteredOrders = if (statusFilter != null) {
                            orders.filter { it.status.name == statusFilter }
                        } else {
                            orders
                        }
                        
                        // Send orders update
                        send(
                            data = json.encodeToString(OrdersUpdateEvent(
                                type = "orders_update",
                                orders = filteredOrders,
                                timestamp = System.currentTimeMillis()
                            )),
                            event = "orders"
                        )
                        
                        // Send heartbeat
                        send(
                            data = json.encodeToString(mapOf("type" to "heartbeat")),
                            event = "heartbeat"
                        )
                        
                        // Wait before next update
                        delay(5000) // Update every 5 seconds
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: java.io.IOException) {
                        if (e.message?.contains("Cannot write to a channel") == true) {
                            logger.debug { "Client disconnected from SSE stream: ${e.message}" }
                            break // Exit the loop gracefully
                        } else {
                            logger.warn { "IO error in SSE stream: ${e.message}" }
                            break
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Error sending SSE update" }
                        // Try to send error event
                        try {
                            send(
                                data = json.encodeToString(mapOf(
                                    "type" to "error",
                                    "message" to e.message
                                )),
                                event = "error"
                            )
                        } catch (sendError: Exception) {
                            // Connection might be closed, log and break
                            logger.debug { "Failed to send error event, connection closed: ${sendError.message}" }
                            break
                        }
                    }
                }
            }
            
            connections[connectionId] = job
            
            // Wait for the job to complete
            job.join()
            
        } catch (e: CancellationException) {
            logger.debug { "SSE connection cancelled for $connectionId" }
        } catch (e: java.io.IOException) {
            if (e.message?.contains("Cannot write to a channel") == true) {
                logger.debug { "SSE connection closed by client: $connectionId" }
            } else {
                logger.warn { "IO error in SSE connection $connectionId: ${e.message}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error in SSE connection $connectionId" }
        } finally {
            connections.remove(connectionId)?.cancel()
            logger.info { "SSE connection closed for $connectionId" }
        }
    }
    
    // Endpoint to trigger manual update (useful for testing)
    post("/orders/events/trigger") {
        val activeConnections = connections.size
        call.respond(HttpStatusCode.OK, mapOf(
            "message" to "Update triggered",
            "activeConnections" to activeConnections
        ))
    }
}

private fun generateConnectionId(): String {
    return "client-${System.currentTimeMillis()}-${(0..1000).random()}"
}

@kotlinx.serialization.Serializable
data class OrdersUpdateEvent(
    val type: String,
    val orders: List<OrderResponse>,
    val timestamp: Long
)
