package co.kandalabs.comandaai.config

import co.kandalabs.comandaai.config.sqldelight.DriverFactory
import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.sdk.session.SessionManagerImpl
import org.kodein.di.DI
import org.kodein.di.bindSingleton

actual val platformDI = DI.Module("desktopModule") {
    bindSingleton<DriverFactory> {
        DriverFactory()
    }
    
    bindSingleton<SessionManager> {
        SessionManagerImpl()
    }
}