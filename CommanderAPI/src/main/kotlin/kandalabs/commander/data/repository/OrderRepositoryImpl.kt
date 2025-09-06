package kandalabs.commander.data.repository

import kandalabs.commander.data.model.sqlModels.ItemTable
import kandalabs.commander.data.model.sqlModels.OrderItemStatusTable
import kandalabs.commander.data.model.sqlModels.OrderItemTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.data.model.sqlModels.TableTable
import kandalabs.commander.data.model.sqlModels.UserTable
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
    val tableTable: TableTable,
    val userTable: UserTable,
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

    override suspend fun getOrderByIdWithStatuses(id: Int): OrderWithStatusesResponse? {
        logger.debug { "Fetching order by id with statuses: $id" }
        return transaction {
            val orderRow = orderTable.selectAll().where { orderTable.id eq id }.singleOrNull()
                ?: return@transaction null

            val order = orderRow.toOrder()

            // Build individual statuses map
            val individualStatuses = mutableMapOf<String, ItemStatus>()

            order.items.forEach { item ->
                val itemId = item.itemId
                val totalCount = item.count
                logger.debug { "Processing item $itemId with count $totalCount" }

                // Get unit statuses for each unit of this item
                (0 until totalCount).forEach { unitIndex ->
                    val statusRow = orderItemStatusTable.selectAll()
                        .where {
                            (orderItemStatusTable.orderId eq id) and
                                    (orderItemStatusTable.itemId eq itemId) and
                                    (orderItemStatusTable.unitIndex eq unitIndex)
                        }
                        .singleOrNull()

                    val status = if (statusRow != null) {
                        val statusValue = ItemStatus.valueOf(statusRow[orderItemStatusTable.status])
                        logger.debug { "Found status record for item $itemId unit $unitIndex: $statusValue" }
                        statusValue
                    } else {
                        logger.debug { "No status record for item $itemId unit $unitIndex, defaulting to PENDING" }
                        ItemStatus.PENDING // Default status
                    }

                    individualStatuses["${itemId}_${unitIndex}"] = status
                }
            }
            
            logger.debug { "Final individual statuses map: $individualStatuses" }

            // Calculate overall order status based on individual item statuses
            val orderStatus = calculateOrderStatus(individualStatuses.values.toList())

            OrderWithStatusesResponse(
                id = order.id,
                billId = order.billId,
                tableNumber = order.tableNumber,
                userName = order.userName,
                items = order.items,
                status = orderStatus,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                individualStatuses = individualStatuses
            )
        }
    }

    override suspend fun getOrdersWithIncompleteItems(): Result<List<KitchenOrder>> {
        logger.debug { "Fetching orders with incomplete items for active table bills" }
        return try {
            val orders = transaction {
                orderTable.join(tableTable, JoinType.INNER, orderTable.tableId, tableTable.id)
                    .selectAll()
                    .where {
                        tableTable.billId.isNotNull() and (orderTable.billId eq tableTable.billId)
                    }
                    .map { orderRow ->
                        val orderId = orderRow[OrderTable.id]
                        val orderUserName = orderRow[OrderTable.userName]

                        // Get user name from UserTable
                        val actualUserName = userTable.selectAll()
                            .where { userTable.userName eq orderUserName }
                            .singleOrNull()
                            ?.get(userTable.name) ?: orderUserName

                        val items = OrderItemTable.selectAll()
                            .where { OrderItemTable.orderId eq orderId }
                            .map { itemRow ->
                                val itemId = itemRow[OrderItemTable.itemId]
                                val totalCount = itemRow[OrderItemTable.count]
                                val itemName = itemRow[OrderItemTable.name]
                                val observation = itemRow[OrderItemTable.observation]

                                // Get item category from ItemTable
                                val itemCategory = ItemTable.selectAll()
                                    .where { ItemTable.id eq itemId }
                                    .singleOrNull()
                                    ?.get(ItemTable.category)
                                    ?.let { ItemCategory.valueOf(it) }
                                    ?: ItemCategory.DRINK

                                // Get unit statuses
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
                                        ItemUnitStatus(
                                            unitIndex = unitIndex,
                                            status = ItemStatus.PENDING,
                                            updatedAt = localDateTimeAsLong(),
                                            updatedBy = null
                                        )
                                    }
                                }

                                val overallStatus =
                                    calculateOverallStatus(unitStatuses.map { it.status })

                                KitchenItemDetail(
                                    itemId = itemId,
                                    name = itemName,
                                    totalCount = totalCount,
                                    observation = observation,
                                    unitStatuses = unitStatuses,
                                    overallStatus = overallStatus,
                                    category = itemCategory
                                )
                            }

                        KitchenOrder(
                            id = orderId,
                            tableNumber = orderRow[tableTable.number],
                            userName = actualUserName,
                            items = items,
                            createdAt = orderRow[OrderTable.createdAt],
                            updatedAt = orderRow[OrderTable.updatedAt]
                        )
                    }
                    .filter { order ->
                        val hasIncompleteItems = order.items.any { item ->
                            item.unitStatuses.any { status ->
                                status.status != ItemStatus.DELIVERED && status.status != ItemStatus.CANCELED
                            }
                        }
                        logger.debug {
                            if (!hasIncompleteItems && order.items.isNotEmpty())
                                "Filtering out order ${order.id} - all items are complete"
                            else
                                "Including order ${order.id} - has incomplete items"
                        }
                        hasIncompleteItems
                    }
            }
            Result.success(orders)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching orders with incomplete items for active table bills" }
            Result.failure(e)
        }
    }

    override suspend fun getOrdersByBillId(billId: Int): List<OrderResponse> {
        logger.debug { "Fetching orders by billId: $billId" }
        val orderIds = transaction {
            orderTable.selectAll().where { orderTable.billId eq billId }
                .map { it[orderTable.id] }
        }
        
        // Get each order with dynamic status calculation outside transaction
        return orderIds.mapNotNull { orderId ->
            getOrderByIdWithStatuses(orderId)?.let { orderWithStatuses ->
                OrderResponse(
                    id = orderWithStatuses.id,
                    billId = orderWithStatuses.billId,
                    status = orderWithStatuses.status, // Use dynamically calculated status
                    userName = orderWithStatuses.userName,
                    items = orderWithStatuses.items.map { item ->
                        ItemOrder(
                            itemId = item.itemId,
                            orderId = item.orderId,
                            name = item.name,
                            observation = item.observation,
                            count = item.count,
                            status = item.status
                        )
                    },
                    createdAt = orderWithStatuses.createdAt,
                    updatedAt = orderWithStatuses.updatedAt,
                    tableNumber = transaction { 
                        orderTable.selectAll().where { orderTable.id eq orderId }
                            .singleOrNull()?.get(orderTable.tableId) ?: 0
                    },
                )
            }
        }
    }

    override suspend fun createOrder(createOrderRequest: CreateOrderRequest): OrderResponse {
        logger.debug { "Creating new order: $createOrderRequest" }
        return transaction {
            runCatching {
                val currentTime = localDateTimeAsLong()
                val orderId = orderTable.insert {
                    it[status] = OrderStatus.PENDING.name
                    it[tableId] = createOrderRequest.tableId
                    it[billId] = createOrderRequest.billId
                    it[userName] = createOrderRequest.userName
                    it[createdAt] = currentTime
                    it[updatedAt] = currentTime
                } get orderTable.id

                createOrderRequest.items.map { item ->
                    OrderItemTable.insert {
                        it[OrderItemTable.orderId] = orderId
                        it[OrderItemTable.name] = item.name
                        it[OrderItemTable.itemId] = item.itemId
                        it[OrderItemTable.count] = item.count
                        it[OrderItemTable.status] = ItemStatus.PENDING.name
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
                it[updatedAt] = localDateTimeAsLong()
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

    override suspend fun updateOrderWithIndividualStatuses(
        id: Int,
        orderResponse: OrderResponse,
        individualStatuses: Map<String, ItemStatus>,
        updatedBy: String
    ): OrderResponse? {
        logger.debug { "Updating order with individual statuses: $id, statuses: $individualStatuses" }
        return try {
            transaction {
                // Update order status
                val rowsUpdated = orderTable.update({ orderTable.id eq id }) {
                    it[status] = orderResponse.status.name
                    it[updatedAt] = localDateTimeAsLong()
                }

                if (rowsUpdated == 0) {
                    return@transaction null
                }

                individualStatuses.forEach { (key, status) ->
                    val parts = key.split("_")
                    if (parts.size == 2) {
                        val itemId = parts[0].toIntOrNull()
                        val unitIndex = parts[1].toIntOrNull()

                        if (itemId != null && unitIndex != null) {
                            // Check if status record exists
                            val existingStatus = orderItemStatusTable.selectAll()
                                .where {
                                    (orderItemStatusTable.orderId eq id) and
                                            (orderItemStatusTable.itemId eq itemId) and
                                            (orderItemStatusTable.unitIndex eq unitIndex)
                                }
                                .singleOrNull()

                            if (existingStatus != null) {
                                // Update existing record
                                orderItemStatusTable.update(
                                    {
                                        (orderItemStatusTable.orderId eq id) and
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
                                    it[orderItemStatusTable.orderId] = id
                                    it[orderItemStatusTable.itemId] = itemId
                                    it[orderItemStatusTable.unitIndex] = unitIndex
                                    it[orderItemStatusTable.status] = status.name
                                    it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                                    it[orderItemStatusTable.updatedBy] = updatedBy
                                    it[orderItemStatusTable.orderItemId] = id * 1000 + itemId
                                }
                            }

                            // Update overall item status based on individual statuses
                            updateOverallItemStatus(id, itemId)
                        }
                    }
                }

                // Update overall order status based on all items after processing individual changes
                updateOverallOrderStatus(id)

                // Return updated order
                orderTable.selectAll().where { orderTable.id eq id }
                    .map { it.toOrder() }
                    .singleOrNull()
            }
        } catch (e: Exception) {
            logger.error(e) { "Error updating order with individual statuses" }
            null
        }
    }

    override suspend fun deleteOrder(id: Int): Boolean {
        logger.debug { "Deleting order with id: $id" }
        return transaction {
            orderTable.deleteWhere { orderTable.id eq id } > 0
        }
    }

    // Kitchen-specific methods
    suspend fun getOrdersWithIncompleteItems2(): Result<List<KitchenOrder>> {
        logger.debug { "Fetching orders with incomplete items for kitchen" }
        return try {
            val orders = transaction {
                // Get all orders except those with GRANTED status
                orderTable.selectAll()
                    .where { orderTable.status neq OrderStatus.DELIVERED.name }
                    .map { orderRow ->
                        val orderId = orderRow[OrderTable.id]
                        val orderUserName = orderRow[OrderTable.userName]

                        // Get user name from UserTable
                        val actualUserName = userTable.selectAll()
                            .where { userTable.userName eq orderUserName }
                            .singleOrNull()
                            ?.get(userTable.name)
                            ?: orderUserName // fallback to userName if user not found

                        val items = OrderItemTable.selectAll()
                            .where { OrderItemTable.orderId eq orderId }
                            .map { itemRow ->
                                val itemId = itemRow[OrderItemTable.itemId]
                                val totalCount = itemRow[OrderItemTable.count]
                                val itemName = itemRow[OrderItemTable.name]
                                val observation = itemRow[OrderItemTable.observation]

                                // Get item category from ItemTable
                                val itemCategory = ItemTable.selectAll()
                                    .where { ItemTable.id eq itemId }
                                    .singleOrNull()
                                    ?.get(ItemTable.category)
                                    ?.let { ItemCategory.valueOf(it) }
                                    ?: ItemCategory.DRINK // default fallback

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
                                            status = ItemStatus.PENDING,
                                            updatedAt = localDateTimeAsLong(),
                                            updatedBy = null
                                        )
                                    }
                                }

                                val overallStatus =
                                    calculateOverallStatus(unitStatuses.map { it.status })

                                KitchenItemDetail(
                                    itemId = itemId,
                                    name = itemName,
                                    totalCount = totalCount,
                                    observation = observation,
                                    unitStatuses = unitStatuses,
                                    overallStatus = overallStatus,
                                    category = itemCategory
                                )
                            }

                        KitchenOrder(
                            id = orderId,
                            tableNumber = orderRow[OrderTable.tableId],
                            userName = actualUserName,
                            items = items,
                            createdAt = orderRow[OrderTable.createdAt],
                            updatedAt = orderRow[OrderTable.updatedAt]
                        )
                    }
                    .filter { order ->
                        // Only include orders that have incomplete items
                        val hasIncompleteItems = order.items.any { item ->
                            item.unitStatuses.any { status ->
                                status.status != ItemStatus.DELIVERED && status.status != ItemStatus.CANCELED
                            }
                        }
                        // Debug log to understand filtering
                        if (!hasIncompleteItems && order.items.isNotEmpty()) {
                            println("Filtering out order ${order.id} - all items are complete")
                        }
                        hasIncompleteItems
                    }
            }
            Result.success(orders)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching orders with incomplete items" }
            Result.failure(e)
        }
    }

    override suspend fun getOrdersWithDeliveredItems(): Result<List<KitchenOrder>> {
        logger.debug { "Fetching orders with delivered items for kitchen (today only)" }
        return try {
            val orders = transaction {
                // Calculate start and end of today in milliseconds (with timezone adjustment)
                val now = System.currentTimeMillis()
                val timezoneOffset = java.util.TimeZone.getDefault().rawOffset
                val localNow = now + timezoneOffset
                val startOfDay =
                    localNow - (localNow % (24 * 60 * 60 * 1000)) - timezoneOffset // Start of today in local time
                val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1 // End of today

                // Get orders from last 2 days (include DELIVERED orders for delivered items view)
                val twoDaysAgo = startOfDay - (24 * 60 * 60 * 1000) // Include yesterday for testing
                orderTable.selectAll()
                    .where {
                        (orderTable.createdAt greaterEq twoDaysAgo) and
                                (orderTable.createdAt lessEq endOfDay)
                    }
                    .orderBy(orderTable.updatedAt to SortOrder.DESC)
                    .map { orderRow ->
                        val orderId = orderRow[OrderTable.id]
                        val orderUserName = orderRow[OrderTable.userName]

                        // Get user name from UserTable
                        val actualUserName = userTable.selectAll()
                            .where { userTable.userName eq orderUserName }
                            .singleOrNull()
                            ?.get(userTable.name)
                            ?: orderUserName // fallback to userName if user not found

                        val items = OrderItemTable.selectAll()
                            .where { OrderItemTable.orderId eq orderId }
                            .map { itemRow ->
                                val itemId = itemRow[OrderItemTable.itemId]
                                val totalCount = itemRow[OrderItemTable.count]
                                val itemName = itemRow[OrderItemTable.name]
                                val observation = itemRow[OrderItemTable.observation]

                                // Get item category from ItemTable
                                val itemCategory = ItemTable.selectAll()
                                    .where { ItemTable.id eq itemId }
                                    .singleOrNull()
                                    ?.get(ItemTable.category)
                                    ?.let { ItemCategory.valueOf(it) }
                                    ?: ItemCategory.DRINK // default fallback

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
                                            status = ItemStatus.PENDING,
                                            updatedAt = localDateTimeAsLong(),
                                            updatedBy = null
                                        )
                                    }
                                }

                                val overallStatus =
                                    calculateOverallStatus(unitStatuses.map { it.status })

                                KitchenItemDetail(
                                    itemId = itemId,
                                    name = itemName,
                                    totalCount = totalCount,
                                    observation = observation,
                                    unitStatuses = unitStatuses,
                                    overallStatus = overallStatus,
                                    category = itemCategory
                                )
                            }

                        KitchenOrder(
                            id = orderId,
                            tableNumber = orderRow[OrderTable.tableId],
                            userName = actualUserName,
                            items = items,
                            createdAt = orderRow[OrderTable.createdAt],
                            updatedAt = orderRow[OrderTable.updatedAt]
                        )
                    }
                    .filter { order ->
                        // Only include orders that have all items delivered
                        order.items.isNotEmpty() && order.items.all { item ->
                            item.unitStatuses.all { status ->
                                status.status == ItemStatus.DELIVERED
                            }
                        }
                    }
            }
            Result.success(orders)
        } catch (e: Exception) {
            logger.error(e) { "Error fetching orders with delivered items" }
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
                // Validate item and unitIndex
                val itemRow = OrderItemTable.selectAll()
                    .where {
                        (OrderItemTable.orderId eq orderId) and
                                (OrderItemTable.itemId eq itemId)
                    }
                    .singleOrNull() ?: run {
                    logger.warn { "Item not found for orderId=$orderId, itemId=$itemId" }
                    return@transaction false
                }

                val itemCount = itemRow[OrderItemTable.count]
                if (unitIndex !in 0 until itemCount) {
                    logger.warn { "Invalid unitIndex=$unitIndex for item with count=$itemCount" }
                    return@transaction false
                }

                // Update or insert unit status in OrderItemStatusTable
                val existingStatus = orderItemStatusTable.selectAll()
                    .where {
                        (orderItemStatusTable.orderId eq orderId) and
                                (orderItemStatusTable.itemId eq itemId) and
                                (orderItemStatusTable.unitIndex eq unitIndex)
                    }
                    .singleOrNull()

                if (existingStatus != null) {
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
                    orderItemStatusTable.insert {
                        it[orderItemStatusTable.orderId] = orderId
                        it[orderItemStatusTable.itemId] = itemId
                        it[orderItemStatusTable.unitIndex] = unitIndex
                        it[orderItemStatusTable.status] = status.name
                        it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                        it[orderItemStatusTable.updatedBy] = updatedBy
                        it[orderItemStatusTable.orderItemId] = orderId * 1000 + itemId
                    }
                }

                updateOverallItemStatus(orderId, itemId)
                true
            }.let { success ->
                Result.success(success)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error updating item unit status for orderId=$orderId, itemId=$itemId, unitIndex=$unitIndex" }
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
                            // Check if existing status is CANCELED - if so, don't change it
                            val currentStatus = ItemStatus.valueOf(existingStatus[orderItemStatusTable.status])
                            if (currentStatus != ItemStatus.CANCELED) {
                                // Update existing record only if not canceled
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

                // Update order status directly to DELIVERED
                OrderTable.update({ OrderTable.id eq orderId }) {
                    it[OrderTable.status] = OrderStatus.DELIVERED.name
                    it[OrderTable.updatedAt] = localDateTimeAsLong()
                }
                
                // Also update overall order status after marking all items as delivered
                updateOverallOrderStatus(orderId)
                
                true
            }.let { success ->
                Result.success(success)
            }
        } catch (e: Exception) {
            logger.error(e) { "Error marking order as delivered" }
            Result.failure(e)
        }
    }

    override suspend fun markItemAsDelivered(
        orderId: Int,
        itemId: Int,
        updatedBy: String
    ): Result<Boolean> {
        return try {
            val success = transaction {
                // Get item count to update all units
                val itemRow = OrderItemTable.selectAll()
                    .where {
                        (OrderItemTable.orderId eq orderId) and
                                (OrderItemTable.itemId eq itemId)
                    }
                    .singleOrNull()
                    ?: return@transaction false // Retorna false se o item não for encontrado

                val itemCount = itemRow[OrderItemTable.count]

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
                        // Check if existing status is CANCELED - if so, don't change it
                        val currentStatus = ItemStatus.valueOf(existingStatus[orderItemStatusTable.status])
                        if (currentStatus != ItemStatus.CANCELED) {
                            // Update existing record only if not canceled
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
            logger.error(e) { "Error marking item as delivered for orderId=$orderId, itemId=$itemId" }
            Result.failure(e)
        }
    }

    /**
     * Calculate overall status based on individual unit statuses
     */
    private fun calculateOverallStatus(unitStatuses: List<ItemStatus>): ItemStatus {
        return when {
            unitStatuses.all { it == ItemStatus.CANCELED } -> ItemStatus.CANCELED
            unitStatuses.all { it == ItemStatus.DELIVERED || it == ItemStatus.CANCELED } -> ItemStatus.DELIVERED
            unitStatuses.any { it == ItemStatus.PENDING } -> ItemStatus.PENDING
            else -> ItemStatus.PENDING
        }
    }

    /**
     * Calculate overall order status based on all item statuses
     */
    private fun calculateOrderStatus(individualStatuses: List<ItemStatus>): OrderStatus {
        logger.debug { "calculateOrderStatus - individual statuses: $individualStatuses" }
        val result = when {
            individualStatuses.isEmpty() -> {
                logger.debug { "calculateOrderStatus - empty statuses, returning PENDING" }
                OrderStatus.PENDING
            }
            individualStatuses.all { it == ItemStatus.DELIVERED } -> {
                logger.debug { "calculateOrderStatus - all delivered, returning DELIVERED" }
                OrderStatus.DELIVERED
            }
            individualStatuses.all { it == ItemStatus.CANCELED } -> {
                logger.debug { "calculateOrderStatus - all canceled, returning CANCELED" }
                OrderStatus.CANCELED // Order is canceled if all items are canceled
            }
            else -> {
                logger.debug { "calculateOrderStatus - mixed statuses, returning PENDING" }
                OrderStatus.PENDING
            }
        }
        logger.debug { "calculateOrderStatus - final result: $result" }
        return result
    }

    /**
     * Update overall item status based on all unit statuses
     */
    private fun updateOverallItemStatus(orderId: Int, itemId: Int) {
        val itemRow = OrderItemTable.selectAll()
            .where {
                (OrderItemTable.orderId eq orderId) and
                        (OrderItemTable.itemId eq itemId)
            }
            .singleOrNull() ?: run {
            logger.warn { "Item not found for orderId=$orderId, itemId=$itemId" }
            return
        }

        val expectedCount = itemRow[OrderItemTable.count]
        val unitStatuses = orderItemStatusTable.selectAll()
            .where {
                (orderItemStatusTable.orderId eq orderId) and
                        (orderItemStatusTable.itemId eq itemId)
            }
            .map { ItemStatus.valueOf(it[orderItemStatusTable.status]) }
            .toList()

        if (unitStatuses.size < expectedCount) {
            logger.warn { "Item orderId=$orderId, itemId=$itemId has ${unitStatuses.size} status records, expected $expectedCount" }
            // Consider item incomplete if not all units have status records
            OrderItemTable.update(
                { (OrderItemTable.orderId eq orderId) and (OrderItemTable.itemId eq itemId) }
            ) {
                it[OrderItemTable.status] = ItemStatus.PENDING.name
            }
        } else if (unitStatuses.isNotEmpty()) {
            val overallStatus = calculateOverallStatus(unitStatuses)
            OrderItemTable.update(
                { (OrderItemTable.orderId eq orderId) and (OrderItemTable.itemId eq itemId) }
            ) {
                it[OrderItemTable.status] = overallStatus.name
            }
        } else {
            logger.warn { "No unit statuses found for orderId=$orderId, itemId=$itemId" }
            OrderItemTable.update(
                { (OrderItemTable.orderId eq orderId) and (OrderItemTable.itemId eq itemId) }
            ) {
                it[OrderItemTable.status] = ItemStatus.PENDING.name
            }
        }

        updateOverallOrderStatus(orderId)
    }

    private fun updateOverallOrderStatus(orderId: Int) {
        val items = OrderItemTable.selectAll()
            .where { OrderItemTable.orderId eq orderId }
            .toList()

        if (items.isEmpty()) {
            logger.warn { "No items found for orderId=$orderId" }
            OrderTable.update({ OrderTable.id eq orderId }) {
                it[OrderTable.status] = OrderStatus.PENDING.name
                it[OrderTable.updatedAt] = localDateTimeAsLong()
            }
            return
        }

        val hasIncompleteItems = items.any { item ->
            val itemId = item[OrderItemTable.itemId]
            val expectedCount = item[OrderItemTable.count]

            val unitStatuses = orderItemStatusTable.selectAll()
                .where {
                    (orderItemStatusTable.orderId eq orderId) and
                            (orderItemStatusTable.itemId eq itemId)
                }
                .map { ItemStatus.valueOf(it[orderItemStatusTable.status]) }
                .toList()

            // Consider item incomplete if not all units have status records
            unitStatuses.size < expectedCount || unitStatuses.any { status ->
                status != ItemStatus.DELIVERED && status != ItemStatus.CANCELED
            }
        }

        // Calculate the new order status based on all individual item statuses
        val newOrderStatus = if (hasIncompleteItems) {
            OrderStatus.PENDING
        } else {
            // All items are complete (either delivered or canceled)
            // Collect all individual statuses to determine order status
            val allIndividualStatuses = mutableListOf<ItemStatus>()
            
            items.forEach { item ->
                val itemId = item[OrderItemTable.itemId]
                val expectedCount = item[OrderItemTable.count]
                
                val unitStatuses = orderItemStatusTable.selectAll()
                    .where {
                        (orderItemStatusTable.orderId eq orderId) and
                                (orderItemStatusTable.itemId eq itemId)
                    }
                    .map { ItemStatus.valueOf(it[orderItemStatusTable.status]) }
                    .toList()
                
                allIndividualStatuses.addAll(unitStatuses)
            }
            
            // Use the same logic as calculateOrderStatus
            when {
                allIndividualStatuses.isEmpty() -> OrderStatus.PENDING
                allIndividualStatuses.all { it == ItemStatus.DELIVERED } -> OrderStatus.DELIVERED
                allIndividualStatuses.all { it == ItemStatus.CANCELED } -> OrderStatus.CANCELED
                else -> OrderStatus.PENDING
            }
        }

        OrderTable.update({ OrderTable.id eq orderId }) {
            it[OrderTable.status] = newOrderStatus.name
            it[OrderTable.updatedAt] = localDateTimeAsLong()
        }
        logger.debug { "Order $orderId updated to status $newOrderStatus" }
    }

    override suspend fun markItemAsPending(
        orderId: Int,
        itemId: Int,
        updatedBy: String
    ): Result<Boolean> {
        return try {
            val success = transaction {
                // Get item count to update all units
                val itemRow = OrderItemTable.selectAll()
                    .where {
                        (OrderItemTable.orderId eq orderId) and
                                (OrderItemTable.itemId eq itemId)
                    }
                    .singleOrNull()
                    ?: return@transaction false // Retorna false se o item não for encontrado

                val itemCount = itemRow[OrderItemTable.count]

                // Update all unit statuses for this item to PENDING
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
                            it[orderItemStatusTable.status] = ItemStatus.PENDING.name
                            it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                            it[orderItemStatusTable.updatedBy] = updatedBy
                        }
                    } else {
                        // Insert new record
                        orderItemStatusTable.insert {
                            it[orderItemStatusTable.orderId] = orderId
                            it[orderItemStatusTable.itemId] = itemId
                            it[orderItemStatusTable.unitIndex] = unitIndex
                            it[orderItemStatusTable.status] = ItemStatus.PENDING.name
                            it[orderItemStatusTable.updatedAt] = localDateTimeAsLong()
                            it[orderItemStatusTable.updatedBy] = updatedBy
                            it[orderItemStatusTable.orderItemId] = orderId * 1000 + itemId
                        }
                    }
                }

                // Update overall item status
                updateOverallItemStatus(orderId, itemId)

                // Check if the order should revert to PENDING or another status
                val hasIncompleteItems = OrderItemTable.selectAll()
                    .where { OrderItemTable.orderId eq orderId }
                    .any { item ->
                        orderItemStatusTable.selectAll()
                            .where {
                                (orderItemStatusTable.orderId eq orderId) and
                                        (orderItemStatusTable.itemId eq item[OrderItemTable.itemId])
                            }
                            .any { statusRow ->
                                val status =
                                    ItemStatus.valueOf(statusRow[orderItemStatusTable.status])
                                status != ItemStatus.DELIVERED && status != ItemStatus.CANCELED
                            }
                    }

                // If there are incomplete items, revert order status to PENDING
                if (hasIncompleteItems) {
                    OrderTable.update({ OrderTable.id eq orderId }) {
                        it[OrderTable.status] =
                            OrderStatus.PENDING.name // Ou outro status apropriado
                        it[OrderTable.updatedAt] = localDateTimeAsLong()
                    }
                    logger.debug { "Order $orderId reverted to PENDING due to incomplete items" }
                } else {
                    logger.debug { "Order $orderId remains unchanged; no incomplete items" }
                }

                true
            }
            Result.success(success)
        } catch (e: Exception) {
            logger.error(e) { "Error marking item as pending for orderId=$orderId, itemId=$itemId" }
            Result.failure(e)
        }
    }
}

public fun ResultRow.toOrder(): OrderResponse {
    val orderId = this[OrderTable.id]
    return OrderResponse(
        id = orderId,
        billId = this[OrderTable.billId],
        status = OrderStatus.valueOf(this[OrderTable.status]),
        userName = this[OrderTable.userName],
        items = transaction {
            OrderItemTable.selectAll().where { OrderItemTable.orderId eq orderId }
                .map { it.toItemOrder() }
        },
        createdAt = this[OrderTable.createdAt].toLocalDateTime(),
        updatedAt = this[OrderTable.updatedAt].toLocalDateTime(),
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
