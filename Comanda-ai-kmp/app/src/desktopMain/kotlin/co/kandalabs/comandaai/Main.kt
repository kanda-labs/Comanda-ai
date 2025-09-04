package co.kandalabs.comandaai

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.kandalabs.comandaai.config.di
import co.kandalabs.comandaai.presentation.ComandaAiApp
import org.kodein.di.compose.withDI

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ComandaAI"
    ) {
        withDI(di) {
            ComandaAiApp()
        }
    }
}