package co.touchlab.dogify.data.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.coroutinesResult.safeRunCatching
import co.touchlab.dogify.data.api.CommanderApi
import co.touchlab.dogify.domain.repository.CreateOrderItemRequest
import co.touchlab.dogify.domain.repository.OrderRepository
import kandalabs.commander.domain.model.Order
import kotlinx.serialization.Serializable

internal class OrderRepositoryImpl(
    private val commanderApi: CommanderApi,
) : OrderRepository {
    override suspend fun createOrder(
        tableId: Int,
        billId: Int,
        items: List<CreateOrderItemRequest>
    ): DogifyResult<Order> =
        safeRunCatching {
            commanderApi.createOrder(
                CreateOrderRequest(
                    tableId = tableId,
                    billId = billId,
                    items = items.map { 
                        CreateOrderItemDto(
                            itemId = it.itemId,
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
    val count: Int,
    val observation: String? = null
)