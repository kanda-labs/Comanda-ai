package co.touchlab.dogify

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.dogify.config.AppModule.initializeKodein
import co.touchlab.dogify.config.sqldelight.DriverFactory
import co.touchlab.dogify.presentation.ComandaAiApp
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.withDI

fun MainViewController() = ComposeUIViewController {
    withDI(
        di = DI.lazy {
            bindSingleton<DriverFactory> {
                DriverFactory()
            }
            initializeKodein()
        }
    ) {
        ComandaAiApp()
    }
}