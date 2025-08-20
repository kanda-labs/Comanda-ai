package co.kandalabs.comandaai.data.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.data.api.CommanderApi
import co.kandalabs.comandaai.data.repository.CreateBillRequest
import co.kandalabs.comandaai.data.repository.UpdateTableRequest
import co.kandalabs.comandaai.domain.repository.TablesRepository
import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TablesRepositoryImp(
    private val commanderApi: CommanderApi,
): TablesRepository {
    override suspend fun getTables(): ComandaAiResult<List<Table>> =
        safeRunCatching {
            commanderApi.getTables()
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

    override suspend fun closeTable(tableId: Int): ComandaAiResult<Unit> {
        return safeRunCatching {
            // First get current table to preserve billId
            val currentTable = commanderApi.getTable(tableId)
            commanderApi.updateTable(
                id = tableId,
                request = UpdateTableRequest(
                    billId = currentTable.billId, // Preserve current billId
                    status = TableStatus.ON_PAYMENT
                )
            )
            Unit // Convert Table return to Unit
        }.onFailure { error ->
            println("Error closing table: $error")
        }
    }

    override suspend fun getBillByTableId(tableId: Int): Bill {
        return  commanderApi.getBillByTableId(tableId)
    }

    override suspend fun finishTablePayment(tableId: Int, billId: Int, totalAmount: Long): ComandaAiResult<Unit> {
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
}