@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package co.kandalabs.comandaai.config.sqldelight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.kandalabs.comandaai.sqldelight.db.ComandaAiDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(ComandaAiDatabase.Schema, "ComandaAiDatabase.db")
    }
}