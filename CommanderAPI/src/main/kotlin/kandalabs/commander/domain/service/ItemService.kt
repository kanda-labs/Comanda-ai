package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.repository.ItemRepository

class ItemService(private val itemRepository: ItemRepository) {

    suspend fun getAllItems(): List<Item> {
        return itemRepository.getAllItems()
    }

    suspend fun getItemById(id: Int): Item? {
        return itemRepository.getItemById(id)
    }

    suspend fun createItem(item: Item): Item {
        return itemRepository.createItem(item)
    }

    suspend fun updateItem(id: Int, item: Item): Item? {
        return itemRepository.updateItem(id, item)
    }

    suspend fun deleteItem(id: Int): Boolean {
        return itemRepository.deleteItem(id)
    }
}
