package co.kandalabs.comandaai.config

import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.features.attendance.config.AttendanceModule
import co.kandalabs.comandaai.kitchen.di.KitchenDIModule
import org.kodein.di.DI

val desktopDI = DI.lazy {
    import(AppModule.appModule)
    import(platformDI)
    import(AttendanceModule.attendanceModule)
    import(AuthModule.authModule)
    import(KitchenDIModule.kitchenModule)
}