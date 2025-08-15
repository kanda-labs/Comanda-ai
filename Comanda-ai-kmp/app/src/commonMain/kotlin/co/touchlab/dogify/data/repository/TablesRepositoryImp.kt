package co.touchlab.dogify.data.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.coroutinesResult.safeRunCatching
import co.touchlab.dogify.data.api.CommanderApi
import co.touchlab.dogify.data.repository.CreateBillRequest
import co.touchlab.dogify.data.repository.UpdateTableRequest
import co.touchlab.dogify.domain.repository.TablesRepository
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus

internal class TablesRepositoryImp(
    private val commanderApi: CommanderApi,
): TablesRepository {
    override suspend fun getTables(): DogifyResult<List<Table>> =
        safeRunCatching {
            commanderApi.getTables()
        }.onFailure { error ->
            println(error)
        }

    override suspend fun getTableById(id: Int): DogifyResult<Table> =
        safeRunCatching {
            commanderApi.getTable(id)
        }.onFailure { error ->
            println("Error fetching table by ID: $error")
        }

    override suspend fun getTableOrders(id: Int): DogifyResult<List<Order>> {
        return safeRunCatching {
            commanderApi.getTable(id).orders
        }.onFailure { error ->
            println(error)
        }
    }

    override suspend fun openTable(tableId: Int, tableNumber: Int): DogifyResult<Unit> {
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

    override suspend fun closeTable(tableId: Int): DogifyResult<Unit> {
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