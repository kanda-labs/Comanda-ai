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
        migration002_UpdateItemStatusesToSimplified()
        migration003_UpdateOrderStatusesToSimplified()
        migration004_CreatePartialPaymentsTable()
        migration005_AddUpdatedAtToOrders()
        migration006_AddUniqueBillConstraint()
        migration007_AddReceivedByToPartialPayments()
        migration008_AddStatusToPartialPayments()
        migration009_AddUserIdColumns()
        migration010_AddPasswordToUsers()

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
    
    /**
     * Migration 002: Update ItemStatus values to simplified system
     * OPEN -> PENDING, GRANTED/IN_PRODUCTION/COMPLETED -> DELIVERED
     */
    private fun migration002_UpdateItemStatusesToSimplified() {
        logger.info { "Running migration 002: Update ItemStatus values to simplified system" }
        
        transaction {
            try {
                // Update order_items table
                val orderItemsUpdated = exec("""
                    UPDATE order_items 
                    SET status = CASE 
                        WHEN status = 'OPEN' THEN 'PENDING'
                        WHEN status IN ('GRANTED', 'IN_PRODUCTION', 'COMPLETED') THEN 'DELIVERED'
                        ELSE status
                    END
                    WHERE status IN ('OPEN', 'GRANTED', 'IN_PRODUCTION', 'COMPLETED')
                """) { rs ->
                    var count = 0
                    while (rs.next()) count++
                    count
                }
                
                // Update order_item_statuses table
                val orderItemStatusesUpdated = exec("""
                    UPDATE order_item_statuses 
                    SET status = CASE 
                        WHEN status = 'OPEN' THEN 'PENDING'
                        WHEN status IN ('GRANTED', 'IN_PRODUCTION', 'COMPLETED') THEN 'DELIVERED'
                        ELSE status
                    END
                    WHERE status IN ('OPEN', 'GRANTED', 'IN_PRODUCTION', 'COMPLETED')
                """) { rs ->
                    var count = 0
                    while (rs.next()) count++
                    count
                }
                
                logger.info { "Updated ItemStatus values in order_items and order_item_statuses tables" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update ItemStatus values: ${e.message}" }
                throw e
            }
        }
    }
    
    /**
     * Migration 003: Update OrderStatus values to simplified system  
     * OPEN -> PENDING, GRANTED -> DELIVERED
     */
    private fun migration003_UpdateOrderStatusesToSimplified() {
        logger.info { "Running migration 003: Update OrderStatus values to simplified system" }
        
        transaction {
            try {
                val ordersUpdated = exec("""
                    UPDATE orders 
                    SET status = CASE 
                        WHEN status = 'OPEN' THEN 'PENDING'
                        WHEN status = 'GRANTED' THEN 'DELIVERED'
                        ELSE status
                    END
                    WHERE status IN ('OPEN', 'GRANTED')
                """) { rs ->
                    var count = 0
                    while (rs.next()) count++
                    count
                }
                
                logger.info { "Updated OrderStatus values in orders table" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to update OrderStatus values: ${e.message}" }
                throw e
            }
        }
    }
    
    /**
     * Migration 004: Create partial_payments table for standalone partial payments
     */
    private fun migration004_CreatePartialPaymentsTable() {
        logger.info { "Running migration 004: Create partial_payments table" }
        
        transaction {
            try {
                // Check if table exists
                val tableExists = exec("SELECT name FROM sqlite_master WHERE type='table' AND name='partial_payments'") { rs ->
                    rs.next()
                } ?: false
                
                if (!tableExists) {
                    // Create the partial_payments table
                    exec("""
                        CREATE TABLE partial_payments (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            bill_id INTEGER NOT NULL,
                            table_id INTEGER NOT NULL,
                            paid_by VARCHAR(255) NOT NULL,
                            amount_in_centavos BIGINT NOT NULL,
                            description VARCHAR(500),
                            payment_method VARCHAR(100),
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (bill_id) REFERENCES bills (id),
                            FOREIGN KEY (table_id) REFERENCES tables (id)
                        )
                    """)
                    logger.info { "Created partial_payments table successfully" }
                } else {
                    logger.info { "partial_payments table already exists, skipping migration" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to create partial_payments table: ${e.message}" }
                throw e
            }
        }
    }
    
    /**
     * Migration 005: Add updated_at column to orders table
     */
    private fun migration005_AddUpdatedAtToOrders() {
        logger.info { "Running migration 005: Add updated_at column to orders table" }
        
        transaction {
            try {
                // Try to select with updated_at column to check if it exists
                exec("SELECT updated_at FROM orders LIMIT 1")
                logger.info { "updated_at column already exists in orders table, skipping migration" }
            } catch (e: Exception) {
                // Column doesn't exist, add it
                try {
                    // Add the column with a default value equal to created_at for existing records
                    exec("ALTER TABLE orders ADD COLUMN updated_at BIGINT NOT NULL DEFAULT 0")
                    
                    // Update existing records to set updated_at = created_at
                    exec("UPDATE orders SET updated_at = created_at WHERE updated_at = 0")
                    
                    logger.info { "Added updated_at column to orders table and initialized with created_at values" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add updated_at column: ${migrationError.message}" }
                    throw migrationError
                }
            }
        }
    }

    /**
     * Migration 006: Add unique constraint to prevent duplicate bills for same table
     */
    private fun migration006_AddUniqueBillConstraint() {
        logger.info { "Running migration 006: Add unique constraint to prevent duplicate bills" }

        transaction {
            try {
                // Check if the unique index already exists
                val indexExists = exec("""
                    SELECT name FROM sqlite_master
                    WHERE type='index' AND name='idx_bills_table_status_unique'
                """) { rs ->
                    rs.next()
                } ?: false

                if (!indexExists) {
                    // Create unique index to prevent multiple active bills for same table
                    // This allows only one bill per table with status OPEN or PARTIALLY_PAID
                    exec("""
                        CREATE UNIQUE INDEX idx_bills_table_status_unique
                        ON bills (table_id)
                        WHERE status IN ('OPEN', 'PARTIALLY_PAID')
                    """)
                    logger.info { "Created unique constraint for bills table to prevent duplicates" }
                } else {
                    logger.info { "Unique constraint for bills already exists, skipping migration" }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to create unique constraint for bills: ${e.message}" }
                throw e
            }
        }
    }

    /**
     * Migration 007: Add received_by column to partial_payments table
     */
    private fun migration007_AddReceivedByToPartialPayments() {
        logger.info { "Running migration 007: Add received_by column to partial_payments table" }

        transaction {
            try {
                // Try to select with received_by column to check if it exists
                exec("SELECT received_by FROM partial_payments LIMIT 1")
                logger.info { "received_by column already exists in partial_payments table, skipping migration" }
            } catch (e: Exception) {
                // Column doesn't exist, add it
                try {
                    exec("ALTER TABLE partial_payments ADD COLUMN received_by VARCHAR(255)")
                    logger.info { "Added received_by column to partial_payments table" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add received_by column: ${migrationError.message}" }
                    throw migrationError
                }
            }
        }
    }

    /**
     * Migration 008: Add status column to partial_payments table
     */
    private fun migration008_AddStatusToPartialPayments() {
        logger.info { "Running migration 008: Add status column to partial_payments table" }

        transaction {
            try {
                // Try to select with status column to check if it exists
                exec("SELECT status FROM partial_payments LIMIT 1")
                logger.info { "status column already exists in partial_payments table, skipping migration" }
            } catch (e: Exception) {
                // Column doesn't exist, add it
                try {
                    exec("ALTER TABLE partial_payments ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PAID'")
                    logger.info { "Added status column to partial_payments table" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add status column: ${migrationError.message}" }
                    throw migrationError
                }
            }
        }
    }

    /**
     * Migration 009: Add user ID columns to bills and partial_payments tables
     */
    private fun migration009_AddUserIdColumns() {
        logger.info { "Running migration 009: Add user ID columns to bills and partial_payments tables" }

        transaction {
            // Add finalized_by_user_id to bills table
            try {
                exec("SELECT finalized_by_user_id FROM bills LIMIT 1")
                logger.info { "finalized_by_user_id column already exists in bills table, skipping" }
            } catch (e: Exception) {
                try {
                    exec("ALTER TABLE bills ADD COLUMN finalized_by_user_id INTEGER")
                    logger.info { "Added finalized_by_user_id column to bills table" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add finalized_by_user_id column: ${migrationError.message}" }
                    throw migrationError
                }
            }

            // Add created_by_user_id to partial_payments table
            try {
                exec("SELECT created_by_user_id FROM partial_payments LIMIT 1")
                logger.info { "created_by_user_id column already exists in partial_payments table, skipping" }
            } catch (e: Exception) {
                try {
                    exec("ALTER TABLE partial_payments ADD COLUMN created_by_user_id INTEGER")
                    logger.info { "Added created_by_user_id column to partial_payments table" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add created_by_user_id column: ${migrationError.message}" }
                    throw migrationError
                }
            }
        }
    }

    /**
     * Migration 010: Add password column to users table
     */
    private fun migration010_AddPasswordToUsers() {
        logger.info { "Running migration 010: Add password column to users table" }

        transaction {
            try {
                // Try to select with password column to check if it exists
                exec("SELECT password FROM users LIMIT 1")
                logger.info { "password column already exists in users table, skipping migration" }
            } catch (e: Exception) {
                // Column doesn't exist, add it
                try {
                    exec("ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '123456'")
                    logger.info { "Added password column to users table with default password '123456'" }
                } catch (migrationError: Exception) {
                    logger.error(migrationError) { "Failed to add password column: ${migrationError.message}" }
                    throw migrationError
                }
            }
        }
    }
}