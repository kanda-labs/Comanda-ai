package kandalabs.commander.data.repository

import kandalabs.commander.domain.model.Table
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kandalabs.commander.domain.model.TableStatus
import kandalabs.commander.domain.repository.TableRepository
import kandalabs.commander.data.model.sqlModels.TableTable
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

