package co.kandalabs.comandaai.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.presentation.screens.tables.listing.TablesScreen

@Composable
fun ComandaAiApp() {
    ComandaAiTheme {
        Navigator(TablesScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}