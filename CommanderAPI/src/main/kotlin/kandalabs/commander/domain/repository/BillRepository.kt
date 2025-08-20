package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.model.PaymentSummaryResponse

interface BillRepository {
    suspend fun getAllBills(billStatus: BillStatus?): List<Bill>
    suspend fun getBillById(id: Int): Bill?
    suspend fun getBillByTableId(tableId: Int): Bill?
    suspend fun getBillPaymentSummary(tableId: Int): PaymentSummaryResponse?
    suspend fun createBill(bill: Bill): Bill
    suspend fun updateBill(id: Int, bill: Bill): Bill?
    suspend fun processTablePayment(tableId: Int): Boolean
    suspend fun deleteBill(id: Int): Boolean
}
