package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus

interface BillRepository {
    suspend fun getAllBills(billStatus: BillStatus?): List<Bill>
    suspend fun getBillById(id: Int): Bill?
    suspend fun createBill(bill: Bill): Bill
    suspend fun updateBill(id: Int, bill: Bill): Bill?
    suspend fun deleteBill(id: Int): Boolean
}
