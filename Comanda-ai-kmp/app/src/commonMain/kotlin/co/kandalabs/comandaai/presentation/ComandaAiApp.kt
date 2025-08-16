package co.kandalabs.comandaai.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.theme.ComandaAiTheme

@Composable
fun ComandaAiApp() {
    ComandaAiTheme {
        Navigator(AuthModule.getLoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}