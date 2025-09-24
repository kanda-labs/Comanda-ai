package co.kandalabs.comandaai.features.attendance.domain.repository

import co.kandalabs.comandaai.sdk.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemStatus

interface ItemsRepository {
    suspend fun getItems(itemStatus: ItemStatus?): ComandaAiResult<List<Item>>
    suspend fun getItemById(id: Int): ComandaAiResult<Item>
    suspend fun createItem(item: Item): ComandaAiResult<Item>
    suspend fun updateItem(id: Int, item: Item): ComandaAiResult<Item>
    suspend fun deleteItem(id: Int): ComandaAiResult<Unit>
}