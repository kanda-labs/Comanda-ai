package co.kandalabs.comandaai.kitchen.data.api

import co.kandalabs.comandaai.sdk.logger.ComandaAiLogger
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.network.HttpClientFactory
import co.kandalabs.comandaai.network.HttpClientType
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class KitchenSSEClient(
    private val json: Json,
    private val baseUrl: String,
    private val logger: ComandaAiLogger
) {
    
    // Test method to check if connection works
    suspend fun testConnection(): Result<String> {
        return try {
            HttpClientFactory.withSafeHttpClient(HttpClientType.API) { httpClient ->
                val response = httpClient.get("${baseUrl}api/v1/kitchen/orders")
                Result.success("Connection test successful: ${response.status}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun connectToKitchenEvents(): Flow<KitchenEvent> = flow {
        logger.d("Starting Kitchen SSE connection to: ${baseUrl}api/v1/kitchen/events")
        try {
            // Create a dedicated SSE client for long-lived connection with SSE plugin
            val sseClient = HttpClient {
                defaultRequest {
                    header("Accept", "text/event-stream")
                    header("Cache-Control", "no-cache")
                }
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
                install(SSE)
            }
            try {
                sseClient.sse(
                    urlString = "${baseUrl}api/v1/kitchen/events",
                    request = {
                        method = HttpMethod.Get
                        val clientId = "kitchen-${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}"
                        parameter("clientId", clientId)
                        logger.d("Kitchen SSE request configured with clientId parameter: $clientId")
                    }
                ) {
                    logger.d("Kitchen SSE connection established, waiting for events...")
                    incoming.collect { event ->
                        logger.d("Kitchen SSE received event: ${event.event} with data: ${event.data}")
                        when (event.event) {
                            "connection" -> {
                                logger.d("Kitchen SSE connection established: ${event.data}")
                                emit(KitchenEvent.Connected(event.data ?: ""))
                            }
                            "kitchen_orders" -> {
                                try {
                                    logger.d("Kitchen SSE parsing kitchen_orders event")
                                    logger.d("Kitchen SSE raw data: ${event.data}")
                                    val ordersUpdate = json.decodeFromString<KitchenOrdersUpdateEvent>(
                                        event.data ?: "{}"
                                    )
                                    logger.d("Kitchen SSE parsed ${ordersUpdate.orders.size} orders")
                                    logger.d("Kitchen SSE orders: ${ordersUpdate.orders}")
                                    emit(KitchenEvent.OrdersUpdate(ordersUpdate.orders))
                                } catch (e: Exception) {
                                    logger.e(e, "Failed to parse kitchen orders update")
                                    logger.e(e, "Kitchen SSE raw data causing error: ${event.data}")
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
            } finally {
                // Close the SSE client when the flow completes or is cancelled
                sseClient.close()
                logger.d("Kitchen SSE client closed")
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
internal data class KitchenOrdersUpdateEvent(
    val type: String,
    val orders: List<KitchenOrder>,
    val timestamp: Long
)