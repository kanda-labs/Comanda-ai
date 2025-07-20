package kandalabs.commander.data.repository

import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemCategory
import kandalabs.commander.domain.repository.ItemRepository
import kandalabs.commander.data.model.sqlModels.ItemTable
import mu.KLogger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll


internal class ItemRepositoryImpl(
    val itemTable: ItemTable,
    val logger: KLogger
) : ItemRepository {
    override suspend fun getAllItems(): List<Item> {
        logger.debug { "Fetching all items" }
        return transaction {
            itemTable.selectAll().map { it.toItem() }
        }
    }

    override suspend fun getItemById(id: Int): Item? {
        logger.debug { "Fetching item by id: $id" }
        return transaction {
            itemTable.selectAll().where { itemTable.id eq id }
                .map { it.toItem() }
                .singleOrNull()
        }
    }

    override suspend fun createItem(item: Item): Item {
        logger.debug { "Creating new item: $item" }
        return transaction {
            val insertStatement = itemTable.insert {
                it[name] = item.name
                it[value] = item.value
                it[category] = item.category.name
                it[description] = item.description
            }
            item.copy(id = insertStatement[itemTable.id])
        }

    }

    override suspend fun updateItem(id: Int, item: Item): Item? {
        logger.debug { "Updating item with id: $id" }
        return transaction {
            val rowsUpdated = itemTable.update({ itemTable.id eq id }) {
                it[name] = item.name
                it[value] = item.value
                it[category] = item.category.name
                it[description] = item.description
            }
            if (rowsUpdated > 0) {
                itemTable.selectAll().where { itemTable.id eq id }
                    .map { it.toItem() }
                    .singleOrNull()
            } else {
                null
            }
        }
    }

    override suspend fun deleteItem(id: Int): Boolean {
        logger.debug { "Deleting item with id: $id" }
        return transaction {
            itemTable.deleteWhere { itemTable.id eq id } > 0
        }
    }

    private fun ResultRow.toItem(): Item {
        return Item(
            id = this[itemTable.id],
            name = this[itemTable.name],
            value = this[itemTable.value],
            category = ItemCategory.valueOf(this[itemTable.category]),
            description = this[itemTable.description],
        )
    }
}
