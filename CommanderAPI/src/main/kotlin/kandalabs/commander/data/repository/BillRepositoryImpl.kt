package kandalabs.commander.data.repository

import kandalabs.commander.core.extensions.getOrThrow
import kandalabs.commander.data.model.sqlModels.BillTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.repository.BillRepository
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
}

internal fun Long.toLocalDateTime() = Instant.fromEpochMilliseconds(this)
    .toLocalDateTime(TimeZone.currentSystemDefault())
