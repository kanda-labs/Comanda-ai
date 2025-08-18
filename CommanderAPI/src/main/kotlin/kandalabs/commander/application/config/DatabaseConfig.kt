package kandalabs.commander.application.config

import kandalabs.commander.data.model.sqlModels.BillTable
import kandalabs.commander.data.model.sqlModels.ItemTable
import kandalabs.commander.data.model.sqlModels.OrderItemTable
import kandalabs.commander.data.model.sqlModels.OrderItemStatusTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.data.model.sqlModels.TableTable
import kandalabs.commander.data.model.sqlModels.UserTable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

/**
 * Configuration for database connection and initialization.
 * Supports environment-based configuration.
 */
object DatabaseConfig {
    
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:data.db"
        val databaseDriver = System.getenv("DATABASE_DRIVER") ?: "org.sqlite.JDBC"
        
        logger.info { "Initializing database connection with URL: $databaseUrl" }
        
        try {
            Database.connect(databaseUrl, driver = databaseDriver)
            
            transaction {
                SchemaUtils.create(UserTable)
                SchemaUtils.create(BillTable)
                SchemaUtils.create(TableTable)
                SchemaUtils.create(OrderTable)
                SchemaUtils.create(OrderItemTable)
                SchemaUtils.create(OrderItemStatusTable)
                SchemaUtils.create(ItemTable)
                logger.info { "Database schema created/verified successfully" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize database: ${e.message}" }
            throw RuntimeException("Database initialization failed", e)
        }
    }
}

