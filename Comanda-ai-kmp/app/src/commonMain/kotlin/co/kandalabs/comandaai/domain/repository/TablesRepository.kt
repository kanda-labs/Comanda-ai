package co.kandalabs.comandaai.domain.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.models.model.Bill
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.models.model.PartialPayment
import co.kandalabs.comandaai.domain.models.model.Table
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
    suspend fun createPartialPayment(tableId: Int, paidBy: String, amountInCentavos: Long, description: String? = null): ComandaAiResult<PartialPayment>
}