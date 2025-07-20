package kandalabs.commander.data.repository

import kandalabs.commander.data.model.sqlModels.OrderItemTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.domain.model.*
import kandalabs.commander.domain.repository.OrderRepository
import kandalabs.commander.presentation.models.request.CreateOrderRequest
import kandalabs.commander.presentation.routes.localDateTimeAsLong
import mu.KLogger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class OrderRepositoryImpl(
    val orderTable: OrderTable,
    val orderItemTable: OrderItemTable,
    val logger: KLogger
) : OrderRepository {
    override suspend fun getAllOrders(): List<OrderResponse> {
        logger.debug { "Fetching all orders" }
        return transaction {
            orderTable.selectAll().map { it.toOrder() }
        }
    }

    override suspend fun getOrderById(id: Int): OrderResponse? {
        logger.debug { "Fetching order by id: $id" }
        return transaction {
            orderTable.selectAll().where { orderTable.id eq id }
                .map { it.toOrder() }
                .singleOrNull()
        }
    }

    override suspend fun getOrdersByBillId(billId: Int): List<OrderResponse> {
        logger.debug { "Fetching orders by billId: $billId" }
        return transaction {
            orderTable.selectAll().where { orderTable.billId eq billId }
                .map { it.toOrder() }
        }
    }

    override suspend fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse {
        logger.debug { "Creating new order: $createOrderRequest" }
        return transaction {
            runCatching {
                val orderId = orderTable.insert {
                    it[status] = OrderStatus.OPEN.name
                    it[tableId] = createOrderRequest.tableId
                    it[billId] = createOrderRequest.billId
                    it[createdAt] = localDateTimeAsLong()
                } get orderTable.id

                createOrderRequest.items.map { item ->
                    OrderItemTable.insert {
                        it[OrderItemTable.orderId] = orderId
                        it[OrderItemTable.name] = item.name
                        it[OrderItemTable.itemId] = item.itemId
                        it[OrderItemTable.count] = item.count
                        it[OrderItemTable.status] = ItemStatus.OPEN.name
                        it[OrderItemTable.observation] = item.observation
                    }
                }

                val orderItemsResponse = transaction {
                    orderItemTable.selectAll().where { OrderItemTable.orderId eq orderId }
                        .map { it.toItemOrder() }
                }

                val order = orderTable.selectAll().where { orderTable.id eq orderId }
                    .map { it.toOrder() }
                    .single()
                    .copy(items = orderItemsResponse)

                order
            }.fold(
                onSuccess = { it },
                onFailure = {
                    logger.error(it) { "Error creating order $it" }
                    throw it
                }
            )
        }
    }

    override suspend fun updateOrder(id: Int, orderResponse: OrderResponse): OrderResponse? {
        logger.debug { "Updating order with id: $id" }
        return transaction {
            val rowsUpdated = orderTable.update({ orderTable.id eq id }) {
                it[status] = orderResponse.status.name
            }
            if (rowsUpdated > 0) {
                orderTable.selectAll().where { orderTable.id eq id }
                    .map { it.toOrder() }
                    .singleOrNull()
            } else {
                null
            }
        }
    }

    override suspend fun deleteOrder(id: Int): Boolean {
        logger.debug { "Deleting order with id: $id" }
        return transaction {
            orderTable.deleteWhere { orderTable.id eq id } > 0
        }
    }
}

public fun ResultRow.toOrder(): OrderResponse {
    val orderId = this[OrderTable.id]
    return OrderResponse(
        id = orderId,
        billId = this[OrderTable.billId],
        status = OrderStatus.valueOf(this[OrderTable.status]),
        items = transaction {
            OrderItemTable.selectAll().where { OrderItemTable.orderId eq orderId }
                .map { it.toItemOrder() }
        },
        createdAt = this[OrderTable.createdAt].toLocalDateTime(),
        tableNumber = this[OrderTable.tableId],
    )
}

private fun ResultRow.toItemOrder(): ItemOrder {
    return ItemOrder(
        itemId = this[OrderItemTable.itemId],
        orderId = this[OrderItemTable.orderId],
        name = this[OrderItemTable.name],
        observation = this[OrderItemTable.observation],
        count = this[OrderItemTable.count],
        status = this[OrderItemTable.status].let { ItemStatus.valueOf(it) }
    )
}
