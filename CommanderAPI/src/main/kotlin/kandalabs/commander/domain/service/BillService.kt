package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.model.PartialPayment
import kandalabs.commander.domain.model.PaymentMethod
import kandalabs.commander.domain.model.PaymentSummaryResponse
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

    suspend fun getBillPaymentSummary(tableId: Int): PaymentSummaryResponse? {
        return billRepository.getBillPaymentSummary(tableId)
    }

    suspend fun createBill(bill: Bill): Bill {
        return billRepository.createBill(bill)
    }

    suspend fun updateBill(id: Int, bill: Bill): Bill? {
        return billRepository.updateBill(id, bill)
    }

    suspend fun processTablePayment(tableId: Int, finalizedByUserId: Int?): Boolean {
        return billRepository.processTablePayment(tableId, finalizedByUserId)
    }

    suspend fun createPartialPayment(partialPayment: PartialPayment, createdByUserId: Int?): PartialPayment {
        return billRepository.createPartialPayment(partialPayment, createdByUserId)
    }

    suspend fun getPartialPayments(tableId: Int): List<PartialPayment> {
        return billRepository.getPartialPayments(tableId)
    }

    suspend fun getPartialPaymentsByUserId(
        userId: Int,
        startDate: Long? = null,
        endDate: Long? = null,
        paymentMethod: PaymentMethod? = null
    ): List<PartialPayment> {
        return billRepository.getPartialPaymentsByUserId(userId, startDate, endDate, paymentMethod)
    }

    suspend fun getPartialPaymentDetails(paymentId: Int): PartialPayment? {
        return billRepository.getPartialPaymentDetails(paymentId)
    }

    suspend fun cancelPartialPayment(paymentId: Int): Boolean {
        return billRepository.cancelPartialPayment(paymentId)
    }

    suspend fun deleteBill(id: Int): Boolean {
        return billRepository.deleteBill(id)
    }
}
