package co.kandalabs.comandaai

import android.app.Application
import android.content.Context
import co.kandalabs.comandaai.config.commonModule
import co.kandalabs.comandaai.config.platformDI
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.kitchen.di.KitchenDIModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class ComandaAiApplication : Application(), DIAware {
    override val di: DI = DI.lazy {
        import(commonModule)
        import(AuthModule.authModule)
        import(KitchenDIModule.kitchenModule)
        import(platformDI)
        bindSingleton<Context> {
            applicationContext
        }
    }
}
