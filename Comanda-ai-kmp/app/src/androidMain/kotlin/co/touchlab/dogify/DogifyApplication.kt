package co.touchlab.dogify

import android.app.Application
import android.content.Context
import co.touchlab.dogify.config.AppModule.initializeKodein
import co.touchlab.dogify.config.sqldelight.DriverFactory
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance

class DogifyApplication : Application(), DIAware {
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
