package co.touchlab.dogify.domain.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table

interface TablesRepository {
    suspend fun getTables(): DogifyResult<List<Table>>
    suspend fun getTableOrders(id: Int): DogifyResult<List<Order>>
}