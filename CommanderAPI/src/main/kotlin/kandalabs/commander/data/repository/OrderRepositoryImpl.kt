package kandalabs.commander.data.repository

import kandalabs.commander.data.model.sqlModels.OrderItemStatusTable
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
    val orderItemStatusTable: OrderItemStatusTable,
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

    // Kitchen-specific methods
    override suspend fun getOrdersWithIncompleteItems(): Result<List<KitchenOrder>> {
        logger.debug { "Fetching orders with incomplete items for kitchen" }
        return try {
            val orders = transaction {
                // Get all orders that have items not fully completed
                orderTable.selectAll()
                    .map { orderRow ->
                        val orderId = orderRow[OrderTable.id]
                        val items = OrderItemTable.selectAll()
                            .where { OrderItemTable.orderId eq orderId }
                            .map { itemRow ->
                                val itemId = itemRow[OrderItemTable.itemId]
                                val totalCount = itemRow[OrderItemTable.count]
                                val itemName = itemRow[OrderItemTable.name]
                                val observation = itemRow[OrderItemTable.observation]
                                
                                // Get unit statuses from OrderItemStatusTable
                                val unitStatuses = (0 until totalCount).map { unitIndex ->
                                    val statusRow = orderItemStatusTable.selectAll()
                                        .where { 
                                            (orderItemStatusTable.orderId eq orderId) and 
                                            (orderItemStatusTable.itemId eq itemId) and 
                                            (orderItemStatusTable.unitIndex eq unitIndex)
                                        }
                                        .singleOrNull()
                                    
                                    if (statusRow != null) {
                                        ItemUnitStatus(
                                            unitIndex = unitIndex,
                                            status = ItemStatus.valueOf(statusRow[orderItemStatusTable.status]),
                                            updatedAt = statusRow[orderItemStatusTable.updatedAt],
                                            updatedBy = statusRow[orderItemStatusTable.updatedBy]
                                        )
                                    } else {
                                        // Create default status if not exists
                                        ItemUnitStatus(
                                            unitIndex = unitIndex,
                                            status = ItemStatus.OPEN,
                                            updatedAt = localDateTimeAsLong(),
                                            updatedBy = null
                                        )
                                    }
                                }
                                
                                // Calculate overall status based on unit statuses
                                val overallStatus = calculateOverallStatus(unitStatuses.map { it.status })
                                
                                KitchenItemDetail(
                                    itemId = itemId,
                                    name = itemName,
                                    totalCount = totalCount,
                                    observation = observation,
                                    unitStatuses = unitStatuses,
                                    overallStatus = overallStatus
                                )
                            }
                        
                        KitchenOrder(
                            id = orderId,
                            tableNumber = orderRow[OrderTable.tableId],
                            items = items,
                            createdAt = orderRow[OrderTable.createdAt]
                        )
                    }
                    .filter { order ->
                        // Only include orders that have incomplete items
                        order.items.any { item ->
                            item.unitStatuses.any { status ->
                                status.status != ItemStatus.DELIVERED && status.status != ItemStatus.CANCELED
                            }
                        }
                    }
            }
            Result.success(orders)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching orders with incomplete items" }
            Result.failure(e)
        }
    }

    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus,
        updatedBy: String
    ): Result<Boolean> {
        logger.debug { "Updating item unit status: orderId=$orderId, itemId=$itemId, unitIndex=$unitIndex, status=$status" }
        return try {
            transaction {
                // Update or insert unit status in OrderItemStatusTable
                val existingStatus = orderItemStatusTable.selectAll()
                    .where { 
                        (orderItemStatusTable.orderId eq orderId) and 
                        (orderItemStatusTable.itemId eq itemId) and 
                        (orderItemStatusTable.unitIndex eq unitIndex)
                    }
                    .singleOrNull()
                
                if (existingStatus != null) {
                    // Update existing record
                    orderItemStatusTable.update(
                        { 
                            (orderItemStatusTable.orderId eq orderId) and 
                            (orderItemStatusTable.itemId eq itemId) and 
                            (orderItemStatusTable.unitIndex eq unitIndex)
                        }
                    ) {
                        it[orderItemStatusTable.status] = status.name
                        it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                        it[orderItemStatusTable.updatedBy] = updatedBy
                    }
                } else {
                    // Insert new record
                    orderItemStatusTable.insert {
                        it[orderItemStatusTable.orderId] = orderId
                        it[orderItemStatusTable.itemId] = itemId
                        it[orderItemStatusTable.unitIndex] = unitIndex
                        it[orderItemStatusTable.status] = status.name
                        it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                        it[orderItemStatusTable.updatedBy] = updatedBy
                        it[orderItemStatusTable.orderItemId] = orderId * 1000 + itemId // Simple composite ID
                    }
                }
                
                // Update overall item status based on all unit statuses
                updateOverallItemStatus(orderId, itemId)
                
                true
            }.let { success ->
                Result.success(success)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error updating item unit status" }
            Result.failure(e)
        }
    }

    override suspend fun getItemUnitStatuses(
        orderId: Int,
        itemId: Int
    ): Result<List<ItemUnitStatus>> {
        logger.debug { "Fetching item unit statuses: orderId=$orderId, itemId=$itemId" }
        return try {
            val statuses = transaction {
                // For now, return mock data based on the item count
                // In a full implementation, this would query OrderItemStatusTable
                val item = OrderItemTable.selectAll()
                    .where { (OrderItemTable.orderId eq orderId) and (OrderItemTable.itemId eq itemId) }
                    .singleOrNull()
                
                if (item != null) {
                    val count = item[OrderItemTable.count]
                    val status = ItemStatus.valueOf(item[OrderItemTable.status])
                    
                    (0 until count).map { unitIndex ->
                        ItemUnitStatus(
                            unitIndex = unitIndex,
                            status = status,
                            updatedAt = localDateTimeAsLong(),
                            updatedBy = null
                        )
                    }
                } else {
                    emptyList()
                }
            }
            Result.success(statuses)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching item unit statuses" }
            Result.failure(e)
        }
    }

    override suspend fun markOrderAsDelivered(orderId: Int, updatedBy: String): Result<Boolean> {
        logger.debug { "Marking entire order as delivered: orderId=$orderId" }
        return try {
            transaction {
                // Get all items in the order
                val orderItems = OrderItemTable.selectAll()
                    .where { OrderItemTable.orderId eq orderId }
                    .toList()
                
                if (orderItems.isEmpty()) {
                    throw IllegalArgumentException("Order not found: $orderId")
                }
                
                // Update all items to DELIVERED status
                orderItems.forEach { item ->
                    val itemId = item[OrderItemTable.itemId]
                    val count = item[OrderItemTable.count]
                    
                    // Update main item status
                    OrderItemTable.update(
                        { (OrderItemTable.orderId eq orderId) and (OrderItemTable.itemId eq itemId) }
                    ) {
                        it[OrderItemTable.status] = ItemStatus.DELIVERED.name
                    }
                    
                    // Update all unit statuses to DELIVERED
                    for (unitIndex in 0 until count) {
                        val existingStatus = orderItemStatusTable.selectAll()
                            .where { 
                                (orderItemStatusTable.orderId eq orderId) and 
                                (orderItemStatusTable.itemId eq itemId) and 
                                (orderItemStatusTable.unitIndex eq unitIndex)
                            }
                            .singleOrNull()
                        
                        if (existingStatus != null) {
                            // Update existing record
                            orderItemStatusTable.update(
                                { 
                                    (orderItemStatusTable.orderId eq orderId) and 
                                    (orderItemStatusTable.itemId eq itemId) and 
                                    (orderItemStatusTable.unitIndex eq unitIndex)
                                }
                            ) {
                                it[orderItemStatusTable.status] = ItemStatus.DELIVERED.name
                                it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                                it[orderItemStatusTable.updatedBy] = updatedBy
                            }
                        } else {
                            // Insert new record
                            orderItemStatusTable.insert {
                                it[orderItemStatusTable.orderId] = orderId
                                it[orderItemStatusTable.itemId] = itemId
                                it[orderItemStatusTable.unitIndex] = unitIndex
                                it[orderItemStatusTable.status] = ItemStatus.DELIVERED.name
                                it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                                it[orderItemStatusTable.updatedBy] = updatedBy
                                it[orderItemStatusTable.orderItemId] = orderId * 1000 + itemId
                            }
                        }
                    }
                }
                
                true
            }.let { success ->
                Result.success(success)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error marking order as delivered" }
            Result.failure(e)
        }
    }
    
    override suspend fun markItemAsDelivered(orderId: Int, itemId: Int, updatedBy: String): Result<Boolean> {
        return try {
            val success = transaction {
                // Get item count to update all units
                val itemCount = OrderItemTable.selectAll()
                    .where { 
                        (OrderItemTable.orderId eq orderId) and 
                        (OrderItemTable.itemId eq itemId) 
                    }
                    .single()[OrderItemTable.count]
                
                // Update all unit statuses for this item
                for (unitIndex in 0 until itemCount) {
                    val existingStatus = orderItemStatusTable.selectAll()
                        .where { 
                            (orderItemStatusTable.orderId eq orderId) and 
                            (orderItemStatusTable.itemId eq itemId) and 
                            (orderItemStatusTable.unitIndex eq unitIndex)
                        }
                        .singleOrNull()
                    
                    if (existingStatus != null) {
                        // Update existing record
                        orderItemStatusTable.update(
                            {
                                (orderItemStatusTable.orderId eq orderId) and 
                                (orderItemStatusTable.itemId eq itemId) and
                                (orderItemStatusTable.unitIndex eq unitIndex)
                            }
                        ) {
                            it[orderItemStatusTable.status] = ItemStatus.DELIVERED.name
                            it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                            it[orderItemStatusTable.updatedBy] = updatedBy
                        }
                    } else {
                        // Insert new record
                        orderItemStatusTable.insert {
                            it[orderItemStatusTable.orderId] = orderId
                            it[orderItemStatusTable.itemId] = itemId
                            it[orderItemStatusTable.unitIndex] = unitIndex
                            it[orderItemStatusTable.status] = ItemStatus.DELIVERED.name
                            it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                            it[orderItemStatusTable.updatedBy] = updatedBy
                            it[orderItemStatusTable.orderItemId] = orderId * 1000 + itemId
                        }
                    }
                }
                
                // Update overall item status
                updateOverallItemStatus(orderId, itemId)
                
                true
            }
            Result.success(success)
        } catch (e: Exception) {
            logger.error(e) { "Error marking item as delivered" }
            Result.failure(e)
        }
    }
    
    /**
     * Calculate overall status based on individual unit statuses
     */
    private fun calculateOverallStatus(unitStatuses: List<ItemStatus>): ItemStatus {
        return when {
            unitStatuses.all { it == ItemStatus.DELIVERED } -> ItemStatus.DELIVERED
            unitStatuses.all { it == ItemStatus.CANCELED } -> ItemStatus.CANCELED
            unitStatuses.all { it == ItemStatus.COMPLETED } -> ItemStatus.COMPLETED
            unitStatuses.any { it == ItemStatus.IN_PRODUCTION } -> ItemStatus.IN_PRODUCTION
            unitStatuses.any { it == ItemStatus.OPEN } -> ItemStatus.OPEN
            else -> ItemStatus.OPEN
        }
    }
    
    /**
     * Update overall item status based on all unit statuses
     */
    private fun updateOverallItemStatus(orderId: Int, itemId: Int) {
        // Get all unit statuses for this item
        val unitStatuses = orderItemStatusTable.selectAll()
            .where { 
                (orderItemStatusTable.orderId eq orderId) and 
                (orderItemStatusTable.itemId eq itemId)
            }
            .map { ItemStatus.valueOf(it[orderItemStatusTable.status]) }
        
        if (unitStatuses.isNotEmpty()) {
            val overallStatus = calculateOverallStatus(unitStatuses)
            
            // Update main item status
            OrderItemTable.update(
                { (OrderItemTable.orderId eq orderId) and (OrderItemTable.itemId eq itemId) }
            ) {
                it[OrderItemTable.status] = overallStatus.name
            }
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
