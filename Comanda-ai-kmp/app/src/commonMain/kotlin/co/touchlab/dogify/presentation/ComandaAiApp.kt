package co.touchlab.dogify.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.touchlab.dogify.presentation.designSystem.theme.ComandaAiTheme
import co.touchlab.dogify.presentation.screens.tables.listing.TablesScreen

@Composable
fun ComandaAiApp() {
    ComandaAiTheme {
        Navigator(TablesScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}