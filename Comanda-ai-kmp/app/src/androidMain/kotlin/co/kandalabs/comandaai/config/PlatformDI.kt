package co.kandalabs.comandaai.config

import android.content.Context
import co.kandalabs.comandaai.config.sqldelight.DriverFactory
import co.kandalabs.comandaai.sdk.cache.CacheManager
import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.sdk.session.SessionManagerImpl
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance

actual val platformDI = DI.Module("AndroidPlatformModule") {
    bindSingleton<DriverFactory> {
        DriverFactory(instance())
    }
    
    bindSingleton<SessionManager> {
        SessionManagerImpl(
            context = instance<Context>(),
            cacheManager = instance<CacheManager>()
        )
    }
}