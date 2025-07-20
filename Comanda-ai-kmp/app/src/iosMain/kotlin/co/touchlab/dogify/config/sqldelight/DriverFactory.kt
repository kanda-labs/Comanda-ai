package co.touchlab.dogify.config.sqldelight

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.dogify.sqldelight.db.DogifyDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(DogifyDatabase.Schema, "dogify.db")
    }
}