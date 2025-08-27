package kandalabs.commander.data.repository

import kandalabs.commander.core.extensions.getOrThrow
import kandalabs.commander.data.model.sqlModels.BillTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.data.model.sqlModels.OrderItemTable
import kandalabs.commander.data.model.sqlModels.OrderItemStatusTable
import kandalabs.commander.data.model.sqlModels.PartialPaymentTable
import kandalabs.commander.data.model.sqlModels.ItemTable
import kandalabs.commander.data.model.sqlModels.TableTable
import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.model.PartialPayment
import kandalabs.commander.domain.model.OrderResponse
import kandalabs.commander.domain.model.OrderStatus
import kandalabs.commander.domain.model.ItemOrder
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.PaymentSummaryResponse
import kandalabs.commander.domain.model.PaymentOrderSummary
import kandalabs.commander.domain.model.PaymentItemSummary
import kandalabs.commander.domain.model.PaymentOrderStatus
import kandalabs.commander.domain.model.TableStatus
import kandalabs.commander.domain.repository.BillRepository
import kandalabs.commander.data.repository.toOrder
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mu.KLogger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import toEpochMilliseconds


class BillRepositoryImpl(
    val billTable: BillTable,
    val orderTable: OrderTable,
    val orderItemTable: OrderItemTable,
    val orderItemStatusTable: OrderItemStatusTable,
    val partialPaymentTable: PartialPaymentTable,
    val itemTable: ItemTable,
    val tableTable: TableTable,
    val logger: KLogger
) : BillRepository {
    override suspend fun getAllBills(billStatus: BillStatus?): List<Bill> {
        return transaction {
            runCatching {

                billTable.selectAll()
                    .let { if (billStatus != null) it.where { billTable.status eq billStatus.name } else it }
                    .map { billFromTable ->
                        val bill = billFromTable.toBill()
                        val orders = orderTable.selectAll().where { orderTable.billId eq bill.id.getOrThrow() }
                            .map { order -> order.toOrder() }
                        bill.copy(orders = orders)
                    }

            }.fold(
                onSuccess = { it },
                onFailure = {
                    logger.error(it) { "Error fetching all bills" }
                    emptyList()
                }
            )
        }
    }

    override suspend fun getBillById(id: Int): Bill? {
        logger.debug { "Fetching bill by id: $id" }
        return transaction {
            billTable.selectAll().where { billTable.id eq id }
                .map { it.toBill() }
                .singleOrNull()
        }
    }

    override suspend fun getBillByTableId(tableId: Int): Bill? {
        logger.debug { "Fetching bill by table id: $tableId" }
        return transaction {
            runCatching {
                billTable.selectAll()
                    .where { 
                        (billTable.tableId eq tableId) and 
                        (billTable.status inList listOf(BillStatus.OPEN.name, BillStatus.PARTIALLY_PAID.name))
                    }
                    .map { billFromTable ->
                        val bill = billFromTable.toBill()
                        val orders = orderTable.selectAll().where { orderTable.billId eq bill.id.getOrThrow() }
                            .map { order -> order.toOrder() }
                        bill.copy(orders = orders)
                    }
                    .singleOrNull()
            }.fold(
                onSuccess = { it },
                onFailure = {
                    logger.error(it) { "Error fetching bill by table id: $tableId" }
                    null
                }
            )
        }
    }

    override suspend fun getBillPaymentSummary(tableId: Int): PaymentSummaryResponse? {
        logger.debug { "Fetching payment summary for table id: $tableId" }
        return transaction {
            runCatching {
                // Get bill info
                logger.debug { "Searching for bill with tableId=$tableId and status in [OPEN, PARTIALLY_PAID, PAID]" }
                val allBills = billTable.selectAll().where { billTable.tableId eq tableId }.toList()
                logger.debug { "Found ${allBills.size} bills for table $tableId: ${allBills.map { "${it[billTable.id]}(${it[billTable.status]})" }}" }
                
                val billRow = billTable.selectAll()
                    .where { 
                        (billTable.tableId eq tableId) and 
                        (billTable.status inList listOf(BillStatus.OPEN.name, BillStatus.PARTIALLY_PAID.name, BillStatus.PAID.name))
                    }
                    .orderBy(billTable.createdAt to SortOrder.DESC)
                    .limit(1)
                    .singleOrNull()
                
                if (billRow == null) {
                    logger.warn { "No bill found for table $tableId with status [OPEN, PARTIALLY_PAID, PAID]" }
                    return@runCatching null
                }
                
                logger.debug { "Found bill: id=${billRow[billTable.id]}, status=${billRow[billTable.status]}" }

                val bill = billRow.toBill()
                val billId = bill.id.getOrThrow()

                // Get all orders for the bill (excluding canceled orders)
                val orders = orderTable.selectAll()
                    .where { (orderTable.billId eq billId) and (orderTable.status neq OrderStatus.CANCELED.name) }
                    .map { orderRow ->
                        val orderId = orderRow[orderTable.id]
                        val orderStatus = OrderStatus.valueOf(orderRow[orderTable.status])

                        // Get all items for this order
                        val allOrderItems = (orderItemTable innerJoin itemTable)
                            .selectAll()
                            .where { orderItemTable.orderId eq orderId }
                        
                        val orderItems = allOrderItems.mapNotNull { itemRow ->
                            val itemId = itemRow[orderItemTable.itemId]
                            val totalQuantity = itemRow[orderItemTable.count]
                            val priceInCentavos = itemRow[itemTable.value]
                            
                            // Count how many units are NOT canceled
                            var validUnits = 0
                            (0 until totalQuantity).forEach { unitIndex ->
                                val statusRow = orderItemStatusTable.selectAll()
                                    .where { 
                                        (orderItemStatusTable.orderId eq orderId) and 
                                        (orderItemStatusTable.itemId eq itemId) and 
                                        (orderItemStatusTable.unitIndex eq unitIndex)
                                    }
                                    .singleOrNull()
                                
                                val status = if (statusRow != null) {
                                    ItemStatus.valueOf(statusRow[orderItemStatusTable.status])
                                } else {
                                    ItemStatus.PENDING // Default status
                                }
                                
                                if (status != ItemStatus.CANCELED) {
                                    validUnits++
                                }
                            }
                            
                            // Only include item if there are valid (non-canceled) units
                            if (validUnits > 0) {
                                val totalInCentavos = validUnits * priceInCentavos.toLong()
                                
                                PaymentItemSummary(
                                    name = itemRow[orderItemTable.name],
                                    quantity = validUnits,
                                    priceInCentavos = priceInCentavos,
                                    priceFormatted = formatCurrency(priceInCentavos.toLong()),
                                    totalInCentavos = totalInCentavos,
                                    totalFormatted = formatCurrency(totalInCentavos),
                                    observation = itemRow[orderItemTable.observation]
                                )
                            } else {
                                null // Exclude completely canceled items
                            }
                        }

                        val orderTotalInCentavos = orderItems.sumOf { it.totalInCentavos }

                        PaymentOrderSummary(
                            id = "Pedido Nº ${orderId}",
                            items = orderItems,
                            orderTotalInCentavos = orderTotalInCentavos,
                            orderTotalFormatted = formatCurrency(orderTotalInCentavos),
                            status = when (orderStatus) {
                                OrderStatus.DELIVERED -> PaymentOrderStatus("Entregue", "#4CAF50")
                                OrderStatus.PENDING -> PaymentOrderStatus("Pendente", "#2196F3")
                                OrderStatus.CANCELED -> PaymentOrderStatus("Cancelado", "#F44336")
                            }
                        )
                    }

                val totalAmountInCentavos = orders.sumOf { it.orderTotalInCentavos }
                
                // Get partial payments for this bill (not table)
                val partialPayments = getPartialPaymentsByBillId(billId)
                val totalPaidInCentavos = partialPayments.sumOf { it.amountInCentavos }
                val remainingAmountInCentavos = maxOf(0, totalAmountInCentavos - totalPaidInCentavos)

                PaymentSummaryResponse(
                    tableNumber = bill.tableNumber.toString().padStart(2, '0'),
                    totalAmountInCentavos = totalAmountInCentavos,
                    totalAmountFormatted = formatCurrency(totalAmountInCentavos),
                    totalPaidInCentavos = totalPaidInCentavos,
                    totalPaidFormatted = formatCurrency(totalPaidInCentavos),
                    remainingAmountInCentavos = remainingAmountInCentavos,
                    remainingAmountFormatted = formatCurrency(remainingAmountInCentavos),
                    orders = orders,
                    partialPayments = partialPayments
                )

            }.fold(
                onSuccess = { it },
                onFailure = {
                    logger.error(it) { "Error fetching payment summary for table id: $tableId" }
                    null
                }
            )
        }
    }

    private fun formatCurrency(amountInCentavos: Long): String {
        val reais = amountInCentavos / 100
        val centavos = amountInCentavos % 100
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }

    override suspend fun createBill(bill: Bill): Bill {
        logger.debug { "Creating new bill: $bill" }
        return transaction {
            val insertStatement = billTable.insert {
                it[tableId] = bill.tableId
                it[tableNumber] = bill.tableNumber
                it[status] = bill.status.name
                it[createdAt] = bill.createdAt.toEpochMilliseconds()
            }
            val generatedId = insertStatement[billTable.id]

            bill.copy(id = generatedId)
        }
    }

    override suspend fun updateBill(id: Int, bill: Bill): Bill? {
        logger.debug { "Updating bill with id: $id" }
        return transaction {
            val rowsUpdated = billTable.update({ billTable.id eq id }) {
                it[tableId] = bill.tableId
                it[tableNumber] = bill.tableNumber
                it[status] = bill.status.name
            }
            if (rowsUpdated > 0) {
                billTable.selectAll().where { billTable.id eq id }
                    .map { it.toBill() }
                    .singleOrNull()
            } else {
                null
            }
        }
    }

    override suspend fun processTablePayment(tableId: Int): Boolean {
        logger.info { "Processing payment for table id: $tableId" }
        return transaction {
            runCatching {
                // Find bill for this table (OPEN or PARTIALLY_PAID)
                val billRow = billTable.selectAll()
                    .where { 
                        (billTable.tableId eq tableId) and 
                        (billTable.status inList listOf(BillStatus.OPEN.name, BillStatus.PARTIALLY_PAID.name))
                    }
                    .singleOrNull()

                if (billRow == null) {
                    logger.warn { "No active bill found for table id: $tableId" }
                    return@runCatching false
                }

                val billId = billRow[billTable.id]
                logger.info { "Found bill $billId for table $tableId, updating to PAID" }

                // Update bill status to PAID
                val billUpdated = billTable.update({ billTable.id eq billId }) {
                    it[status] = BillStatus.PAID.name
                }
                logger.info { "Bill update result: $billUpdated rows affected" }

                // Get all orders for this bill and update them to DELIVERED
                val orders = orderTable.selectAll()
                    .where { orderTable.billId eq billId }
                    .map { it[orderTable.id] }

                logger.info { "Found ${orders.size} orders to mark as DELIVERED" }
                orders.forEach { orderId ->
                    val orderUpdated = orderTable.update({ orderTable.id eq orderId }) {
                        it[status] = OrderStatus.DELIVERED.name
                    }
                    logger.debug { "Order $orderId update result: $orderUpdated rows affected" }
                }

                // Update table status to CLOSED and clear billId
                val tableUpdated = tableTable.update({ tableTable.id eq tableId }) {
                    it[status] = TableStatus.CLOSED.name
                    it[tableTable.billId] = null
                }
                logger.info { "Table $tableId update result: $tableUpdated rows affected" }
                
                true
            }.fold(
                onSuccess = { 
                    logger.info { "Successfully processed payment for table id: $tableId" }
                    it 
                },
                onFailure = {
                    logger.error(it) {
                        "Error processing payment for table id: $tableId ---> ${it.message}"
                    }
                    false
                }
            )
        }
    }

    override suspend fun createPartialPayment(partialPayment: PartialPayment): PartialPayment {
        logger.debug { "Creating partial payment for table id: ${partialPayment.tableId}" }
        return transaction {
            // Insert partial payment record
            val insertStatement = partialPaymentTable.insert {
                it[billId] = partialPayment.billId
                it[tableId] = partialPayment.tableId
                it[paidBy] = partialPayment.paidBy
                it[amountInCentavos] = partialPayment.amountInCentavos
                it[description] = partialPayment.description
                it[paymentMethod] = partialPayment.paymentMethod
                it[createdAt] = System.currentTimeMillis()
            }
            val generatedId = insertStatement[partialPaymentTable.id]

            // Update bill status to PARTIALLY_PAID (never automatically set to PAID)
            // Only "Finalizar pagamento" action should set status to PAID
            billTable.update({ billTable.id eq partialPayment.billId }) {
                it[status] = BillStatus.PARTIALLY_PAID.name
            }

            partialPayment.copy(id = generatedId)
        }
    }

    override suspend fun getPartialPayments(tableId: Int): List<PartialPayment> {
        logger.debug { "Getting partial payments for table id: $tableId" }
        return transaction {
            // Get current bill for this table first
            val billRow = billTable.selectAll()
                .where { 
                    (billTable.tableId eq tableId) and 
                    (billTable.status inList listOf(BillStatus.OPEN.name, BillStatus.PARTIALLY_PAID.name))
                }
                .singleOrNull()
            
            if (billRow == null) {
                emptyList()
            } else {
                val billId = billRow[billTable.id]
                getPartialPaymentsByBillId(billId)
            }
        }
    }
    
    private fun getPartialPaymentsByBillId(billId: Int): List<PartialPayment> {
        return partialPaymentTable.selectAll()
            .where { partialPaymentTable.billId eq billId }
            .orderBy(partialPaymentTable.createdAt, SortOrder.DESC)
            .map { it.toPartialPayment() }
    }

    private fun getBillTotalAmount(billId: Int): Long {
        val orders = orderTable.selectAll()
            .where { orderTable.billId eq billId }
            .map { it[orderTable.id] }

        return orders.sumOf { orderId ->
            orderItemTable.selectAll()
                .where { orderItemTable.orderId eq orderId }
                .sumOf { itemRow ->
                    val itemId = itemRow[orderItemTable.itemId]
                    val count = itemRow[orderItemTable.count]
                    
                    val itemPrice = itemTable.selectAll()
                        .where { itemTable.id eq itemId }
                        .single()[itemTable.value]
                    
                    itemPrice * count
                }
        }.toLong()
    }

    private fun getPartialPaymentsTotalAmount(tableId: Int): Long {
        return partialPaymentTable.selectAll()
            .where { partialPaymentTable.tableId eq tableId }
            .sumOf { it[partialPaymentTable.amountInCentavos] }
    }
    
    private fun getPartialPaymentsTotalAmountByBillId(billId: Int): Long {
        return partialPaymentTable.selectAll()
            .where { partialPaymentTable.billId eq billId }
            .sumOf { it[partialPaymentTable.amountInCentavos] }
    }

    override suspend fun deleteBill(id: Int): Boolean {
        logger.debug { "Deleting bill with id: $id" }
        val rowsUpdated = billTable.update({ billTable.id eq id }) {
            it[status] = BillStatus.CANCELED.name
        }
        return rowsUpdated > 0
    }

    private fun ResultRow.toBill(): Bill {
        return Bill(
            id = this[billTable.id],
            tableId = this[billTable.tableId],
            tableNumber = this[billTable.tableNumber],
            status = BillStatus.valueOf(this[billTable.status]),
            orders = emptyList(),
            createdAt = this[billTable.createdAt].toLocalDateTime()
        )
    }

    private fun ResultRow.toPartialPayment(): PartialPayment {
        val amountInCentavos = this[partialPaymentTable.amountInCentavos]
        return PartialPayment(
            id = this[partialPaymentTable.id],
            billId = this[partialPaymentTable.billId],
            tableId = this[partialPaymentTable.tableId],
            paidBy = this[partialPaymentTable.paidBy],
            amountInCentavos = amountInCentavos,
            amountFormatted = formatCurrency(amountInCentavos),
            description = this[partialPaymentTable.description],
            paymentMethod = this[partialPaymentTable.paymentMethod],
            createdAt = this[partialPaymentTable.createdAt].toLocalDateTime()
        )
    }
}

internal fun Long.toLocalDateTime() = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())

