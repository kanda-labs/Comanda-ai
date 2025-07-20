@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package co.touchlab.dogify.config.sqldelight

import app.cash.sqldelight.db.SqlDriver
import co.touchlab.dogify.sqldelight.db.DogifyDatabase

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(driverFactory: DriverFactory): DogifyDatabase {
    val driver = driverFactory.createDriver()
    return DogifyDatabase(driver)
}
