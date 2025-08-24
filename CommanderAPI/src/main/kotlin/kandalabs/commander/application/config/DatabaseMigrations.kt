package kandalabs.commander.application.config

import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll

private val logger = KotlinLogging.logger {}

/**
 * Database migrations for schema changes.
 * Each migration should be idempotent and check if the change is already applied.
 */
object DatabaseMigrations {
    
    fun runAllMigrations() {
        logger.info { "Running database migrations..." }
        
        migration001_AddUserNameToOrders()
        
        logger.info { "All database migrations completed successfully" }
    }
    
    /**
     * Migration 001: Add user_name column to orders table
     */
    private fun migration001_AddUserNameToOrders() {
        logger.info { "Running migration 001: Add user_name column to orders table" }
        
        transaction {
            try {
                // Try to select with user_name column to check if it exists
                exec("SELECT user_name FROM orders LIMIT 1")
                logger.info { "user_name column already exists in orders table, skipping migration" }
            } catch (e: Exception) {
                // Column doesn't exist, add it
                try {
                    exec("ALTER TABLE orders ADD COLUMN user_name VARCHAR(255) NOT NULL DEFAULT ''")
                    logger.info { "Added user_name column to orders table" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add user_name column: ${migrationError.message}" }
                    throw migrationError
                }
            }
        }
    }
}