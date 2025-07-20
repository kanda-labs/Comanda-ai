package kandalabs.commander.domain.repository

import kandalabs.commander.domain.model.Item

interface ItemRepository {
    suspend fun getAllItems(): List<Item>
    suspend fun getItemById(id: Int): Item?
    suspend fun createItem(item: Item): Item
    suspend fun updateItem(id: Int, item: Item): Item?
    suspend fun deleteItem(id: Int): Boolean
}
