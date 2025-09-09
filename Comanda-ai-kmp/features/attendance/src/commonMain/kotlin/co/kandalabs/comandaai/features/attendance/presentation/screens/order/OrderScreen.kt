package co.kandalabs.comandaai.features.attendance.presentation.screens.order

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.features.attendance.presentation.screens.feedback.FeedbackScreen
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TableDetailsScreen

class OrderScreen(
    private val tableId: Int,
    private val tableNumber: String,
    private val billId: Int
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel<OrderScreenModel>()
        
        OrderScreenContent(
            tableNumber = tableNumber,
            screenModel = screenModel,
            onBackClick = { navigator.pop() },
            onSubmitOrder = { 
                screenModel.submitOrder(tableId, billId)
            },
            onOrderSuccess = {
                // Navigate back to table details (pop twice: OrderScreen + any FeedbackScreen)
                navigator.popUntil { screen -> screen is TableDetailsScreen }
            },
            onShowFeedback = { isSuccess, message ->
                navigator.push(FeedbackScreen(isSuccess, message))
            }
        )
    }
}