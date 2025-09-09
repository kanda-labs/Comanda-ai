package co.kandalabs.comandaai.features.attendance.domain.repository

import co.kandalabs.comandaai.features.attendance.domain.models.model.Bill
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.features.attendance.domain.models.model.PartialPayment
import co.kandalabs.comandaai.features.attendance.domain.models.model.PaymentSummaryResponse
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult

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
    suspend fun createPartialPayment(tableId: Int, paidBy: String, amountInCentavos: Long, description: String? = null): ComandaAiResult<PartialPayment>
    suspend fun reopenTable(tableId: Int): ComandaAiResult<Unit>
    suspend fun migrateTable(originTableId: Int, destinationTableId: Int): ComandaAiResult<Pair<Table, Table>>
    suspend fun getFreeTables(): ComandaAiResult<List<Table>>
}