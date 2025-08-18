package co.kandalabs.comandaai.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.auth.navigation.AuthNavigationCallback
import co.kandalabs.comandaai.presentation.screens.splash.SplashScreen
import co.kandalabs.comandaai.presentation.screens.tables.listing.TablesScreen
import co.kandalabs.comandaai.theme.ComandaAiTheme

@Composable
fun ComandaAiApp() {
    ComandaAiTheme {
        // Start with splash screen to check for active session
        Navigator(SplashScreen) { navigator ->
            // Configure auth navigation callback
            AuthNavigationCallback.setOnLoginSuccess {
                navigator.replace(TablesScreen)
            }
            
            SlideTransition(navigator)
        }
    }
}