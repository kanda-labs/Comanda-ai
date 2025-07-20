package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus

interface TableRepository {
    suspend fun getAllTables(): List<Table>
    suspend fun getTableById(id: Int): Table?
    suspend fun createTable(table: Table): Table
    suspend fun updateTable(tableId: Int, newBillId: Int?, newStatus: TableStatus?): Table?
    suspend fun deleteTable(id: Int): Boolean
}

