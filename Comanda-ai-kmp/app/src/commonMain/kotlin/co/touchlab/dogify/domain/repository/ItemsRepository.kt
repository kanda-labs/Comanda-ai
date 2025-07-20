package co.touchlab.dogify.domain.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemStatus

interface ItemsRepository {
    suspend fun getItems(itemStatus: ItemStatus?): DogifyResult<List<Item>>
}