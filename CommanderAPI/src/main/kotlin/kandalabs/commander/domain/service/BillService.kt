package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.repository.BillRepository

class BillService(private val billRepository: BillRepository) {

    suspend fun getAllBills(billStatus: BillStatus?): List<Bill> {
        return billRepository.getAllBills(billStatus)
    }

    suspend fun getBillById(id: Int): Bill? {
        return billRepository.getBillById(id)
    }

    suspend fun getBillByTableId(tableId: Int): Bill? {
        return billRepository.getBillByTableId(tableId)
    }

    suspend fun createBill(bill: Bill): Bill {
        return billRepository.createBill(bill)
    }

    suspend fun updateBill(id: Int, bill: Bill): Bill? {
        return billRepository.updateBill(id, bill)
    }

    suspend fun deleteBill(id: Int): Boolean {
        return billRepository.deleteBill(id)
    }
}
