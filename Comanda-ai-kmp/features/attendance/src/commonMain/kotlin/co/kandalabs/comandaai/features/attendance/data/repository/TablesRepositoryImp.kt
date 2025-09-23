package co.kandalabs.comandaai.features.attendance.data.repository

import co.kandalabs.comandaai.sdk.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.sdk.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.features.attendance.data.api.CommanderApi
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.features.attendance.domain.models.model.PaymentSummaryResponse
import co.kandalabs.comandaai.features.attendance.domain.models.model.Bill
import co.kandalabs.comandaai.features.attendance.domain.models.model.BillStatus
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.features.attendance.domain.models.model.PartialPayment
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.models.model.TableStatus
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreatePartialPaymentRequest
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails.PartialPaymentDetails
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TablesRepositoryImp(
    private val commanderApi: CommanderApi,
) : TablesRepository {
    override suspend fun getTables(): ComandaAiResult<List<Table>> =
        safeRunCatching {
            commanderApi.getTablesHome()
        }.onFailure { error ->
            println(error)
        }

    override suspend fun getTableById(id: Int): ComandaAiResult<Table> =
        safeRunCatching {
            commanderApi.getTable(id)
        }.onFailure { error ->
            println("Error fetching table by ID: $error")
        }

    override suspend fun getTableOrders(id: Int): ComandaAiResult<List<Order>> {
        return safeRunCatching {
            commanderApi.getTable(id).orders
        }.onFailure { error ->
            println("Error fetching table orders: $error")
        }
    }

    override suspend fun openTable(tableId: Int, tableNumber: Int): ComandaAiResult<Unit> {
        return safeRunCatching {
            commanderApi.createBill(
                CreateBillRequest(
                    tableId = tableId,
                    tableNumber = tableNumber
                )
            )
        }.onFailure { error ->
            println("Error opening table: $error")
        }
    }

    override suspend fun closeTable(tableId: Int, billId: Int): Table {
        return commanderApi.updateTable(
            id = tableId,
            request = UpdateTableRequest(
                billId = billId,
                status = TableStatus.ON_PAYMENT
            )
        )
    }

    override suspend fun getBillByTableId(tableId: Int): Bill {
        return commanderApi.getBillByTableId(tableId)
    }

    override suspend fun finishTablePayment(
        tableId: Int,
        billId: Int,
        totalAmount: Long
    ): ComandaAiResult<Unit> {
        return safeRunCatching {
            val updatedBill = Bill(
                id = billId,
                tableId = tableId,
                tableNumber = null,
                orders = emptyList(),
                status = BillStatus.PAID,
                createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
            commanderApi.updateBill(billId, updatedBill)

            // Then mark table as FREE and clear billId (null removes the link)
            commanderApi.updateTable(
                id = tableId,
                request = UpdateTableRequest(
                    billId = null,  // Clear the billId to remove link to paid bill
                    status = TableStatus.FREE
                )
            )
            Unit
        }.onFailure { error ->
            println("Error finishing table payment: $error")
        }
    }

    override suspend fun getPaymentSummary(tableId: Int): ComandaAiResult<PaymentSummaryResponse> {
        return safeRunCatching {
            commanderApi.getPaymentSummary(tableId)
        }.onFailure { error ->
            println("Error getting payment summary: $error")
        }
    }

    override suspend fun processTablePayment(tableId: Int, finalizedByUserId: Int?): ComandaAiResult<Unit> {
        return safeRunCatching {
            requireNotNull(finalizedByUserId) { "User ID is required to finalize payment" }
            val request = co.kandalabs.comandaai.features.attendance.domain.models.request.ProcessTablePaymentRequest(
                finalizedByUserId = finalizedByUserId
            )
            commanderApi.processTablePayment(tableId, request)
        }.onFailure { error ->
            println("Error processing table payment: $error")
        }
    }

    override suspend fun createPartialPayment(
        tableId: Int,
        paidBy: String,
        amountInCentavos: Long,
        description: String?,
        paymentMethod: PaymentMethod?,
        receivedBy: String?,
        createdByUserId: Int?
    ): ComandaAiResult<PartialPayment> {
        return safeRunCatching {
            requireNotNull(createdByUserId) { "User ID is required to create partial payment" }
            val request = CreatePartialPaymentRequest(
                paidBy = paidBy,
                amountInCentavos = amountInCentavos,
                description = description,
                paymentMethod = paymentMethod,
                receivedBy = receivedBy,
                createdByUserId = createdByUserId
            )
            commanderApi.createPartialPayment(tableId, request)
        }.onFailure { error ->
            println("Error creating partial payment: $error")
        }
    }

    override suspend fun reopenTable(tableId: Int, billId: Int): Table {
        return commanderApi.updateTable(
            id = tableId,
            request = UpdateTableRequest(
                billId = billId,
                status = TableStatus.OCCUPIED
            )
        )
    }

    override suspend fun migrateTable(
        originTableId: Int,
        destinationTableId: Int
    ): ComandaAiResult<Pair<Table, Table>> {
        return safeRunCatching {
            val response = commanderApi.migrateTable(originTableId, destinationTableId)
            Pair(response.originTable, response.destinationTable)
        }.onFailure { error ->
            println("Error migrating table: $error")
        }
    }

    override suspend fun getFreeTables(): ComandaAiResult<List<Table>> {
        return safeRunCatching {
            commanderApi.getTablesHome().filter { it.status == TableStatus.FREE }
        }.onFailure { error ->
            println("Error fetching free tables: $error")
        }
    }

    override suspend fun getPartialPaymentDetails(paymentId: Int): ComandaAiResult<PartialPaymentDetails> {
        return safeRunCatching {
            commanderApi.getPartialPaymentDetails(paymentId)
        }.onFailure { error ->
            println("Error fetching partial payment details: $error")
        }
    }

    override suspend fun cancelPartialPayment(paymentId: Int): ComandaAiResult<Unit> {
        return safeRunCatching {
            commanderApi.cancelPartialPayment(paymentId)
        }.onFailure { error ->
            println("Error canceling partial payment: $error")
        }
    }

    override suspend fun getPaymentHistory(
        userId: Int,
        startDate: Long?,
        endDate: Long?,
        paymentMethod: PaymentMethod?
    ): ComandaAiResult<List<PartialPayment>> {
        return safeRunCatching {
            commanderApi.getPaymentHistory(
                userId = userId,
                startDate = startDate,
                endDate = endDate,
                paymentMethod = paymentMethod?.name
            )
        }.onFailure { error ->
            println("Error getting payment history for user $userId: $error")
        }
    }
}