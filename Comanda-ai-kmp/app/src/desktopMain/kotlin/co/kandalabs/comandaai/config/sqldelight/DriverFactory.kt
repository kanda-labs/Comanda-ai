package co.kandalabs.comandaai.config.sqldelight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.kandalabs.comandaai.sqldelight.db.ComandaAiDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ComandaAiDatabase.Schema.create(driver)
        return driver
    }
}