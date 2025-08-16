package co.kandalabs.comandaai.data.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.data.api.CommanderApi
import co.kandalabs.comandaai.domain.repository.CreateOrderItemRequest
import co.kandalabs.comandaai.domain.repository.OrderRepository
import kandalabs.commander.domain.model.Order
import kotlinx.serialization.Serializable

internal class OrderRepositoryImpl(
    private val commanderApi: CommanderApi,
) : OrderRepository {
    override suspend fun createOrder(
        tableId: Int,
        billId: Int,
        items: List<CreateOrderItemRequest>
    ): ComandaAiResult<Order> =
        safeRunCatching {
            commanderApi.createOrder(
                CreateOrderRequest(
                    tableId = tableId,
                    billId = billId,
                    items = items.map { 
                        CreateOrderItemDto(
                            itemId = it.itemId,
                            name = it.name,
                            count = it.count,
                            observation = it.observation
                        )
                    }
                )
            )
        }.onFailure { error ->
            println("Error creating order: $error")
        }
}

@Serializable
data class CreateOrderRequest(
    val tableId: Int,
    val billId: Int,
    val items: List<CreateOrderItemDto>
)

@Serializable
data class CreateOrderItemDto(
    val itemId: Int,
    val name: String,
    val count: Int,
    val observation: String? = null
)

@Serializable
data class CreateBillRequest(
    val tableId: Int?,
    val tableNumber: Int?
)

@Serializable
data class UpdateTableRequest(
    val billId: Int? = null,
    val status: kandalabs.commander.domain.model.TableStatus? = null
)