package co.kandalabs.comandaai.features.attendance.presentation.screens.order

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.features.attendance.presentation.screens.feedback.FeedbackScreen
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TableDetailsScreen
import co.kandalabs.comandaai.theme.ComandaAiTheme

class OrderScreen(
    private val tableId: Int,
    private val tableNumber: String,
    private val billId: Int
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<OrderScreenModel>()

        ComandaAiTheme {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ComandaAiTheme.colorScheme.surface
            ) {
                OrderScreenContent(
                    tableNumber = tableNumber,
                    screenModel = screenModel,
                    onBackClick = { navigator.pop() },
                    onOrderSuccess = {
                        navigator.popUntil { screen -> screen is TableDetailsScreen }
                    },
                    onShowFeedback = { isSuccess, message ->
                        navigator.push(FeedbackScreen(isSuccess, message))
                    },
                    onNavigateToConfirmation = {
                        navigator.push(
                            OrderConfirmationScreen(
                                tableNumber = tableNumber,
                                tableId = tableId,
                                billId = billId,
                                screenModel = screenModel
                            )
                        )
                    }
                )
            }
        }
    }
}