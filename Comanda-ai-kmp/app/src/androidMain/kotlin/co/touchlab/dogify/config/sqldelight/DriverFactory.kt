package co.touchlab.dogify.config.sqldelight

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import co.touchlab.dogify.sqldelight.db.DogifyDatabase

actual open class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(DogifyDatabase.Schema, context, "dogify.db")
    }
}