package co.touchlab.dogify.data.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.coroutinesResult.safeRunCatching
import co.touchlab.dogify.data.api.CommanderApi
import co.touchlab.dogify.domain.repository.TablesRepository
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table

internal class TablesRepositoryImp(
    private val commanderApi: CommanderApi,
): TablesRepository {
    override suspend fun getTables(): DogifyResult<List<Table>> =
        safeRunCatching {
            commanderApi.getTables()
        }.onFailure { error ->
            println(error)
        }

    override suspend fun getTableOrders(id: Int): DogifyResult<List<Order>> {
        return safeRunCatching {
            commanderApi.getTable(id).orders
        }.onFailure { error ->
            println(error)
        }
    }
}