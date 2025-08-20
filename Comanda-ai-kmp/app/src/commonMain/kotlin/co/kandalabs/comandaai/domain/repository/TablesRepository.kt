package co.kandalabs.comandaai.domain.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import co.kandalabs.comandaai.domain.models.model.PaymentSummaryResponse

interface TablesRepository {
    suspend fun getTables(): ComandaAiResult<List<Table>>
    suspend fun getTableById(id: Int): ComandaAiResult<Table>
    suspend fun getTableOrders(id: Int): ComandaAiResult<List<Order>>
    suspend fun openTable(tableId: Int, tableNumber: Int): ComandaAiResult<Unit>
    suspend fun closeTable(tableId: Int): ComandaAiResult<Unit>
    suspend fun getBillByTableId(tableId: Int): Bill
    suspend fun getPaymentSummary(tableId: Int): ComandaAiResult<PaymentSummaryResponse>
    suspend fun finishTablePayment(tableId: Int, billId: Int, totalAmount: Long): ComandaAiResult<Unit>
    suspend fun processTablePayment(tableId: Int): ComandaAiResult<Unit>
}