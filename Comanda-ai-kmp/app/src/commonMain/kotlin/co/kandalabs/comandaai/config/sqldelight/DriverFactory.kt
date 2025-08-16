@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package co.kandalabs.comandaai.config.sqldelight

import app.cash.sqldelight.db.SqlDriver
import co.kandalabs.comandaai.sqldelight.db.ComandaAiDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): ComandaAiDatabase {
    val driver = driverFactory.createDriver()
    return ComandaAiDatabase(driver)
}
