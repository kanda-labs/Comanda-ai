package co.kandalabs.comandaai.kitchen.data.api

import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

interface KitchenApi {
    suspend fun getActiveOrders(): List<KitchenOrder>
    suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    )
    suspend fun markOrderAsDelivered(orderId: Int)
    suspend fun markItemAsDelivered(orderId: Int, itemId: Int)
}

class KitchenApiImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : KitchenApi {
    
    override suspend fun getActiveOrders(): List<KitchenOrder> {
        return httpClient.get("$baseUrl/api/v1/kitchen/orders").body()
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ) {
        httpClient.put("$baseUrl/api/v1/kitchen/orders/$orderId/items/$itemId/unit/$unitIndex") {
            contentType(ContentType.Application.Json)
            setBody(UpdateItemStatusRequest(status.name))
        }
    }
    
    override suspend fun markOrderAsDelivered(orderId: Int) {
        httpClient.put("$baseUrl/api/v1/kitchen/orders/$orderId/deliver")
    }
    
    override suspend fun markItemAsDelivered(orderId: Int, itemId: Int) {
        httpClient.put("$baseUrl/api/v1/kitchen/orders/$orderId/items/$itemId/deliver")
    }
}

@Serializable
private data class UpdateItemStatusRequest(val status: String)