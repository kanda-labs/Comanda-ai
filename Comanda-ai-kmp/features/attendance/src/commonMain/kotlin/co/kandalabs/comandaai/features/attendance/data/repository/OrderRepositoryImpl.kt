package co.kandalabs.comandaai.features.attendance.data.repository

import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.sdk.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.sdk.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.features.attendance.domain.models.model.OrderWithStatuses
import co.kandalabs.comandaai.features.attendance.domain.models.model.TableStatus
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateOrderItemRequest
import co.kandalabs.comandaai.features.attendance.domain.repository.OrderRepository

import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.features.attendance.data.api.CommanderApi
import kotlinx.serialization.Serializable

internal class OrderRepositoryImpl(
    private val commanderApi: CommanderApi,
    private val logger: ComandaAiLogger,
) : OrderRepository {
    override suspend fun createOrder(
        tableId: Int,
        billId: Int,
        userName: String,
        items: List<CreateOrderItemRequest>
    ): ComandaAiResult<Order> =
        safeRunCatching {
            commanderApi.createOrder(
                CreateOrderRequest(
                    tableId = tableId,
                    billId = billId,
                    userName = userName,
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
            logger.e(
                error,
                "Error creating order: $error \n data: tableId = $tableId, billId = $billId userName = $userName, items = $items",
            )
        }

    override suspend fun getAllOrders(): ComandaAiResult<List<Order>> =
        safeRunCatching {
            commanderApi.getAllOrders()
        }.onFailure { error ->
            println("Error getting all orders: $error")
        }

    override suspend fun getOrderById(orderId: Int): ComandaAiResult<Order> =
        safeRunCatching {
            commanderApi.getOrderById(orderId)
        }.onFailure { error ->
            println("Error getting order by id: $error")
        }

    override suspend fun getOrderByIdWithStatuses(orderId: Int): ComandaAiResult<OrderWithStatuses> =
        safeRunCatching {
            commanderApi.getOrderByIdWithStatuses(orderId)
        }.onFailure { error ->
            println("Error getting order by id with statuses: $error")
        }

    override suspend fun updateOrder(orderId: Int, order: Order): ComandaAiResult<Order> =
        safeRunCatching {
            commanderApi.updateOrder(orderId, order)
        }.onFailure { error ->
            println("Error updating order: $error")
        }

    override suspend fun updateOrderWithIndividualStatuses(
        orderId: Int,
        order: Order,
        individualStatuses: Map<String, ItemStatus>,
        updatedBy: String
    ): ComandaAiResult<Order> =
        safeRunCatching {
            println("[OrderRepository] Chamando API updateOrderWithIndividualStatuses...")
            println("[OrderRepository] Order ID: $orderId")
            println("[OrderRepository] Individual statuses: $individualStatuses")
            println("[OrderRepository] Updated by: $updatedBy")

            val request = UpdateOrderWithStatusesRequest(
                order = order,
                individualStatuses = individualStatuses,
                updatedBy = updatedBy
            )

            println("[OrderRepository] Request payload: $request")

            val response = commanderApi.updateOrderWithIndividualStatuses(orderId, request)

            println("[OrderRepository] Response received: $response")

            response
        }.onFailure { error ->
            println("[OrderRepository] Error updating order with individual statuses: $error")
            println("[OrderRepository] Error stacktrace: ${error.stackTraceToString()}")
        }
}

@Serializable
data class CreateOrderRequest(
    val tableId: Int,
    val billId: Int,
    val userName: String,
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
data class UpdateOrderWithStatusesRequest(
    val order: Order,
    val individualStatuses: Map<String, ItemStatus>,
    val updatedBy: String
)

@Serializable
data class UpdateTableRequest(
    val billId: Int? = null,
    val status: TableStatus? = null
)