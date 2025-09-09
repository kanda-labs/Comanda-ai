package co.kandalabs.comandaai.features.attendance.domain.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemStatus

interface ItemsRepository {
    suspend fun getItems(itemStatus: ItemStatus?): ComandaAiResult<List<Item>>
}