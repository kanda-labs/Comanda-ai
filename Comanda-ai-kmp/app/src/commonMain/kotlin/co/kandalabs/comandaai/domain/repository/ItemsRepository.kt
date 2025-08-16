package co.kandalabs.comandaai.domain.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemStatus

interface ItemsRepository {
    suspend fun getItems(itemStatus: ItemStatus?): ComandaAiResult<List<Item>>
}