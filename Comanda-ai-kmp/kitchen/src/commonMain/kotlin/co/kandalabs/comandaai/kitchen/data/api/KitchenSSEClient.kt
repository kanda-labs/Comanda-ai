package co.kandalabs.comandaai.kitchen.data.api

import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import io.ktor.client.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class KitchenSSEClient(
    private val httpClient: HttpClient,
    private val json: Json,
    private val baseUrl: String,
    private val logger: ComandaAiLogger
) {
    
    fun connectToKitchenEvents(): Flow<KitchenEvent> = flow {
        try {
            httpClient.sse(
                urlString = "${baseUrl}api/v1/kitchen/events",
                request = {
                    method = HttpMethod.Get
                    parameter("clientId", "kitchen-${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}")
                }
            ) {
                incoming.collect { event ->
                    when (event.event) {
                        "connection" -> {
                            logger.d("Kitchen SSE connection established: ${event.data}")
                            emit(KitchenEvent.Connected(event.data ?: ""))
                        }
                        "kitchen_orders" -> {
                            try {
                                val ordersUpdate = json.decodeFromString<KitchenOrdersUpdateEvent>(
                                    event.data ?: "{}"
                                )
                                emit(KitchenEvent.OrdersUpdate(ordersUpdate.orders))
                            } catch (e: Exception) {
                                logger.e(e, "Failed to parse kitchen orders update")
                                emit(KitchenEvent.Error("Failed to parse orders: ${e.message}"))
                            }
                        }
                        "heartbeat" -> {
                            logger.d("Kitchen heartbeat received")
                            emit(KitchenEvent.Heartbeat)
                        }
                        "error" -> {
                            logger.e(RuntimeException(event.data ?: "Unknown error"), "Kitchen SSE error received")
                            emit(KitchenEvent.Error(event.data ?: "Unknown error"))
                        }
                        else -> {
                            logger.d("Unknown Kitchen SSE event: ${event.event}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.e(e, "Kitchen SSE connection error")
            emit(KitchenEvent.Error("Connection failed: ${e.message}"))
        }
    }
}

sealed class KitchenEvent {
    data class Connected(val connectionId: String) : KitchenEvent()
    data class OrdersUpdate(val orders: List<KitchenOrder>) : KitchenEvent()
    data object Heartbeat : KitchenEvent()
    data class Error(val message: String) : KitchenEvent()
}

@Serializable
private data class KitchenOrdersUpdateEvent(
    val type: String,
    val orders: List<KitchenOrder>,
    val timestamp: Long
)