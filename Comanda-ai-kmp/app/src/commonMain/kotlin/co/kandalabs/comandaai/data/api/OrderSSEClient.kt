package co.kandalabs.comandaai.data.api

import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kandalabs.commander.domain.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class OrderSSEClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
    private val logger: ComandaAiLogger
) {
    
    fun connectToOrderEvents(): Flow<OrderEvent> = flow {
        try {
            httpClient.sse(
                urlString = "${baseUrl}api/v1/orders/events",
                request = {
                    method = HttpMethod.Get
                    parameter("clientId", "kitchen-${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}")
                }
            ) {
                incoming.collect { event ->
                    when (event.event) {
                        "connection" -> {
                            logger.d("SSE connection established: ${event.data}")
                            emit(OrderEvent.Connected(event.data ?: ""))
                        }
                        "orders" -> {
                            try {
                                val ordersUpdate = json.decodeFromString<OrdersUpdateEvent>(
                                    event.data ?: "{}"
                                )
                                emit(OrderEvent.OrdersUpdate(ordersUpdate.orders))
                            } catch (e: Exception) {
                                logger.e(e, "Failed to parse orders update")
                                emit(OrderEvent.Error("Failed to parse orders: ${e.message}"))
                            }
                        }
                        "heartbeat" -> {
                            logger.d("Heartbeat received")
                            emit(OrderEvent.Heartbeat)
                        }
                        "error" -> {
                            logger.e(RuntimeException(event.data ?: "Unknown error"), "SSE error received")
                            emit(OrderEvent.Error(event.data ?: "Unknown error"))
                        }
                        else -> {
                            logger.d("Unknown SSE event: ${event.event}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.e(e, "SSE connection error")
            emit(OrderEvent.Error("Connection failed: ${e.message}"))
        }
    }
}

sealed class OrderEvent {
    data class Connected(val connectionId: String) : OrderEvent()
    data class OrdersUpdate(val orders: List<Order>) : OrderEvent()
    data object Heartbeat : OrderEvent()
    data class Error(val message: String) : OrderEvent()
}

@Serializable
private data class OrdersUpdateEvent(
    val type: String,
    val orders: List<Order>,
    val timestamp: Long
)
