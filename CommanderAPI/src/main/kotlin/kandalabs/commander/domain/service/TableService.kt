package kandalabs.commander.domain.service

import kandalabs.commander.domain.repository.TableRepository
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus

class TableService(private val tableRepository: TableRepository) {

    suspend fun getAllTables(): Result<List<Table>> {
        return try {
            Result.success(tableRepository.getAllTables())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTableById(id: Int): Result<Table?> {
        return try {
            Result.success(tableRepository.getTableById(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTable(table: Table): Result<Table> {
        return try {
            Result.success(tableRepository.createTable(table))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTable(tableId: Int, newBillId: Int?, newStatus: TableStatus?): Result<Table?> {
        return try {
            Result.success(tableRepository.updateTable(tableId, newBillId, newStatus))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTable(id: Int): Result<Boolean> {
        return try {
            Result.success(tableRepository.deleteTable(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
