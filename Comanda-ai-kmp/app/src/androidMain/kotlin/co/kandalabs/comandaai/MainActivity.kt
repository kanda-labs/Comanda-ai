package co.kandalabs.comandaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat.setDecorFitsSystemWindows
import co.kandalabs.comandaai.presentation.ComandaAiApp
import org.kodein.di.compose.withDI

internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDecorFitsSystemWindows(window, false)
        
        val app = application as ComandaAiApplication
        
        setContent {
            withDI(app.di) {
                ComandaAiApp()
            }
        }
    }
}