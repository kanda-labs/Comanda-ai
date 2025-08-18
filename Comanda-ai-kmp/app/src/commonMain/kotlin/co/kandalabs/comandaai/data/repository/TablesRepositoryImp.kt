package co.kandalabs.comandaai.data.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.data.api.CommanderApi
import co.kandalabs.comandaai.data.repository.CreateBillRequest
import co.kandalabs.comandaai.data.repository.UpdateTableRequest
import co.kandalabs.comandaai.domain.repository.TablesRepository
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus

internal class TablesRepositoryImp(
    private val commanderApi: CommanderApi,
): TablesRepository {
    override suspend fun getTables(): ComandaAiResult<List<Table>> =
        safeRunCatching {
            commanderApi.getTables()
        }.onFailure { error ->
            println(error)
        }

    override suspend fun getTableById(id: Int): ComandaAiResult<Table> =
        safeRunCatching {
            commanderApi.getTable(id)
        }.onFailure { error ->
            println("Error fetching table by ID: $error")
        }

    override suspend fun getTableOrders(id: Int): ComandaAiResult<List<Order>> {
        return safeRunCatching {
            commanderApi.getTable(id).orders
        }.onFailure { error ->
            println("Error fetching table orders: $error")
        }
    }

    override suspend fun openTable(tableId: Int, tableNumber: Int): ComandaAiResult<Unit> {
        return safeRunCatching {
            commanderApi.createBill(
                CreateBillRequest(
                    tableId = tableId,
                    tableNumber = tableNumber
                )
            )
        }.onFailure { error ->
            println("Error opening table: $error")
        }
    }

    override suspend fun closeTable(tableId: Int): ComandaAiResult<Unit> {
        return safeRunCatching {
            commanderApi.updateTable(
                id = tableId,
                request = UpdateTableRequest(
                    status = TableStatus.ON_PAYMENT
                )
            )
            Unit // Convert Table return to Unit
        }.onFailure { error ->
            println("Error closing table: $error")
        }
    }
}