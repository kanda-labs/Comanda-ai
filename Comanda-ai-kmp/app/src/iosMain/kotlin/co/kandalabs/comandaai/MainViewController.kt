package co.kandalabs.comandaai

import androidx.compose.ui.window.ComposeUIViewController
import co.kandalabs.comandaai.config.iosDI
import co.kandalabs.comandaai.presentation.ComandaAiApp
import org.kodein.di.compose.withDI

fun MainViewController() = ComposeUIViewController {
    withDI(di = iosDI) {
        ComandaAiApp()
    }
}