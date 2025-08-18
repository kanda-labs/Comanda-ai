package kandalabs.commander.data.model.sqlModels

import kandalabs.commander.data.repository.SQLTable
import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val userName = varchar("user_name", 255).uniqueIndex()
    val email = varchar("email", 255).nullable()
    val active = bool("active").default(true)
    val role = varchar("role", 20).default("WAITER")
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object BillTable : SQLTable("bills") {
    val id = integer("id").autoIncrement()
    val tableId = integer("table_id").nullable()
    val tableNumber = integer("table_number").nullable()
    val status = varchar("status", 32)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object TableTable : SQLTable("tables") {
    val id = integer("id").autoIncrement()
    val billId = integer("bill_id").nullable()
    val number = integer("number")
    val createdAt = long("created_at")
    val status = varchar("status", 32)
    override val primaryKey = PrimaryKey(id)
}

object OrderTable : SQLTable("orders") {
    val id = integer("id").autoIncrement()
    val billId = integer("bill_id").references(BillTable.id)
    val tableId = integer("table_id").references(TableTable.id)
    val status = varchar("status", 32)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ItemTable : SQLTable("items") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val value = integer("value")
    val category = varchar("category", 32)
    val description = varchar("description", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}

object OrderItemTable : Table("order_items") {
    val orderId = integer("order_id").references(OrderTable.id)
    val name = varchar("name", length = 255)
    val itemId = integer("item_id").references(ItemTable.id)
    val count = integer("count")
    val status = varchar("status", 32)
    val observation = varchar("observation", 255).nullable()
    override val primaryKey = PrimaryKey(orderId, itemId)
}

object OrderItemStatusTable : Table("order_item_statuses") {
    val id = integer("id").autoIncrement()
    val orderItemId = integer("order_item_id")
    val itemId = integer("item_id").references(ItemTable.id)
    val orderId = integer("order_id").references(OrderTable.id)
    val unitIndex = integer("unit_index")
    val status = varchar("status", 32)
    val updatedAt = long("updated_at")
    val updatedBy = varchar("updated_by", 255).nullable()
    
    override val primaryKey = PrimaryKey(id)
}


