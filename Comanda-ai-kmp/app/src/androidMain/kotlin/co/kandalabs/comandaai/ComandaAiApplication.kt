package co.kandalabs.comandaai

import android.app.Application
import android.content.Context
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.config.AppModule
import co.kandalabs.comandaai.config.platformDI
import co.kandalabs.comandaai.features.attendance.config.AttendanceModule
import co.kandalabs.comandaai.kitchen.di.KitchenDIModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bind
import org.kodein.di.singleton

class ComandaAiApplication : Application(), DIAware {
    override val di: DI = DI.lazy {
        // Bind Context first
        bind<Context>() with singleton { this@ComandaAiApplication.applicationContext }
        
        // Import platform-specific dependencies first
        import(platformDI)
        
        // Then import other modules
        import(AppModule.appModule)
        import(AttendanceModule.attendanceModule)
        import(AuthModule.authModule)
        import(KitchenDIModule.kitchenModule)
    }
}
