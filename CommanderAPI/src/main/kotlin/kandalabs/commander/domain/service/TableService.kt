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

    suspend fun migrateTable(originId: Int, destinationId: Int): Result<Pair<Table, Table>> {
        return try {
            // Validate origin table exists and is occupied
            val originTable = tableRepository.getTableById(originId)
            if (originTable == null) {
                return Result.failure(IllegalArgumentException("Origin table not found"))
            }

            if (originTable.status != TableStatus.OPEN) {
                return Result.failure(IllegalArgumentException("Origin table is not occupied"))
            }

            if (originTable.billId == null) {
                return Result.failure(IllegalArgumentException("Origin table has no active bill"))
            }

            // Validate destination table exists and is free
            val destinationTable = tableRepository.getTableById(destinationId)
            if (destinationTable == null) {
                return Result.failure(IllegalArgumentException("Destination table not found"))
            }

            if (destinationTable.status != TableStatus.CLOSED) {
                return Result.failure(IllegalArgumentException("Destination table is not free"))
            }

            // Perform migration
            val result = tableRepository.migrateTable(originId, destinationId, originTable.billId)
            
            if (result) {
                // Return updated tables
                val updatedOriginTable = tableRepository.getTableById(originId)!!
                val updatedDestinationTable = tableRepository.getTableById(destinationId)!!
                Result.success(Pair(updatedOriginTable, updatedDestinationTable))
            } else {
                Result.failure(RuntimeException("Failed to migrate table"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
