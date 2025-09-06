package kandalabs.commander.data.repository

import kandalabs.commander.domain.model.Table
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kandalabs.commander.domain.model.TableStatus
import kandalabs.commander.domain.repository.TableRepository
import kandalabs.commander.data.model.sqlModels.TableTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.data.model.sqlModels.PartialPaymentTable
import kandalabs.commander.data.model.sqlModels.BillTable
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import mu.KLogger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import toEpochMilliseconds
import kotlin.let

typealias SQLTable = org.jetbrains.exposed.sql.Table


class TableRepositoryImpl(
    val tableTable: TableTable,
    val logger: KLogger
) : TableRepository {

    override suspend fun getAllTables(): List<Table> {
        logger.debug { "Fetching all tables" }
        return transaction {
            tableTable.selectAll()
                .map { it.toTable() }
        }
    }

    override suspend fun getTableById(id: Int): Table? {
        logger.debug { "Fetching table by id: $id" }
        return transaction {
            tableTable.selectAll().where { tableTable.id eq id }
                .map { it.toTable() }
                .singleOrNull()
        }
    }

    override suspend fun createTable(table: Table): Table {
        logger.debug { "Creating new table: $table" }
        return transaction {
            val insertStatement = tableTable.insert {
                it[billId] = table.billId
                it[number] = table.number
                it[createdAt] = table.createdAt.toEpochMilliseconds()
                it[status] = table.status.name
            }
            val generatedId = insertStatement[tableTable.id]

            table.copy(id = generatedId)
        }
    }

    override suspend fun updateTable(tableId: Int, newBillId: Int?, newStatus: TableStatus?): Table? {
        logger.debug { "Updating table with id: $tableId, newBillId: $newBillId, newStatus: $newStatus" }
        return transaction {
            val rowsUpdated = tableTable.update({ tableTable.id eq tableId }) {
                // Update billId - null value explicitly clears the billId
                it[billId] = newBillId
                newStatus?.let { safeStatus -> it[status] = safeStatus.name }
            }
            if (rowsUpdated > 0) {
                tableTable.selectAll().where { tableTable.id eq tableId }
                    .map { it.toTable() }
                    .singleOrNull()
            } else {
                null
            }
        }
    }

    override suspend fun deleteTable(id: Int): Boolean {
        logger.debug { "Deleting table with id: $id" }
        return transaction {
            tableTable.deleteWhere { tableTable.id eq id } > 0
        }
    }

    override suspend fun migrateTable(originId: Int, destinationId: Int, billId: Int): Boolean {
        logger.debug { "=== TableRepositoryImpl.migrateTable START ===" }
        logger.debug { "Parameters: originId=$originId, destinationId=$destinationId, billId=$billId" }
        
        return try {
            transaction {
                logger.debug { "Starting database transaction for table migration" }
                
                // Update origin table: set status to CLOSED and billId to null
                logger.debug { "Step 1: Updating origin table $originId - setting status to CLOSED and billId to null" }
                val originUpdated = tableTable.update({ tableTable.id eq originId }) {
                    it[tableTable.status] = TableStatus.CLOSED.name
                    it[tableTable.billId] = null
                } > 0
                
                logger.debug { "Origin table update result: $originUpdated (rows affected: ${if (originUpdated) "1" else "0"})" }

                // Update destination table: set status to OPEN and assign billId
                logger.debug { "Step 2: Updating destination table $destinationId - setting status to OPEN and billId to $billId" }
                val destinationUpdated = tableTable.update({ tableTable.id eq destinationId }) {
                    it[tableTable.status] = TableStatus.OPEN.name
                    it[tableTable.billId] = billId
                } > 0
                
                logger.debug { "Destination table update result: $destinationUpdated (rows affected: ${if (destinationUpdated) "1" else "0"})" }

                // Update orders: change tableId from origin to destination for this billId
                logger.debug { "Step 3: Updating orders with billId $billId - changing tableId from $originId to $destinationId" }
                val ordersUpdated = OrderTable.update({ OrderTable.billId eq billId }) {
                    it[OrderTable.tableId] = destinationId
                }
                
                logger.debug { "Orders update result: $ordersUpdated rows affected" }

                // Update partial payments: change tableId from origin to destination for this billId
                logger.debug { "Step 4: Updating partial payments with billId $billId - changing tableId from $originId to $destinationId" }
                val partialPaymentsUpdated = PartialPaymentTable.update({ PartialPaymentTable.billId eq billId }) {
                    it[PartialPaymentTable.tableId] = destinationId
                }
                
                logger.debug { "Partial payments update result: $partialPaymentsUpdated rows affected" }

                // Update bills table: change tableId and tableNumber for this billId
                logger.debug { "Step 5: Updating bills with billId $billId - changing tableId from $originId to $destinationId" }
                
                // Get destination table number first
                val destinationTableNumber = tableTable.selectAll().where { tableTable.id eq destinationId }
                    .singleOrNull()?.let { it[tableTable.number] }
                
                if (destinationTableNumber != null) {
                    val billsUpdated = BillTable.update({ BillTable.id eq billId }) {
                        it[BillTable.tableId] = destinationId
                        it[BillTable.tableNumber] = destinationTableNumber
                    }
                    
                    logger.debug { "Bills update result: $billsUpdated rows affected (tableId=$destinationId, tableNumber=$destinationTableNumber)" }
                } else {
                    logger.error { "Could not find destination table number for tableId $destinationId" }
                }

                // All table updates must succeed
                val result = originUpdated && destinationUpdated
                logger.debug { "Final migration result: $result (originUpdated=$originUpdated && destinationUpdated=$destinationUpdated, ordersUpdated=$ordersUpdated, partialPaymentsUpdated=$partialPaymentsUpdated)" }
                
                if (result) {
                    logger.debug { "=== TableRepositoryImpl.migrateTable SUCCESS ===" }
                } else {
                    logger.error { "=== TableRepositoryImpl.migrateTable FAILED - one or both updates failed ===" }
                }
                
                result
            }
        } catch (e: Exception) {
            logger.error { "EXCEPTION in TableRepositoryImpl.migrateTable: ${e.message}" }
            logger.error { "Exception type: ${e::class.simpleName}" }
            logger.error(e) { "Full exception stack trace" }
            throw e
        }
    }

    /**
     * Maps a database row to a domain Table entity
     */
    private fun ResultRow.toTable(): Table {
        return Table(
            id = this[tableTable.id],
            billId = this[tableTable.billId],
            number = this[tableTable.number],
            createdAt = Instant.fromEpochMilliseconds(this[tableTable.createdAt])
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            status = TableStatus.valueOf(this[tableTable.status])
        )
    }
}

