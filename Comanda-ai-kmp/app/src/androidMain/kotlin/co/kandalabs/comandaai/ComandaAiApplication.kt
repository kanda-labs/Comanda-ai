package co.kandalabs.comandaai

import android.app.Application
import android.content.Context
import co.kandalabs.comandaai.config.AppModule.initializeKodein
import co.kandalabs.comandaai.config.sqldelight.DriverFactory
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class ComandaAiApplication : Application(), DIAware {
    override val di: DI = DI.lazy {
        initializeKodein()
        bindSingleton<Context> {
            applicationContext
        }

        bindSingleton<DriverFactory> {
            DriverFactory(instance())
        }
    }
}
