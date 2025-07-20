package co.touchlab.dogify.presentation.screens.tables.details

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.dogify.core.coroutinesResult.safeRunCatching
import co.touchlab.dogify.domain.repository.TablesRepository
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

internal class TablesDetailsViewModel(
    private val repository: TablesRepository,
) : StateScreenModel<TableDetailsScreenState>(TableDetailsScreenState()) {

    fun setupDetails(table: Table) {
        screenModelScope.launch {
            mutableState.emit(TableDetailsScreenState(isLoading = true))

            safeRunCatching {
                val tableOrders =
                    table.id?.let { repository.getTableOrders(it).getOrElse { emptyList() } } ?: emptyList<Order>()
                val tableWithOrders = table.copy(orders = tableOrders.toPersistentList())
                TableDetailsScreenState(table = tableWithOrders, isLoading = false)
            }.fold(
                onSuccess = { tableDetailsScreenState ->
                    mutableState.emit(tableDetailsScreenState)
                },
                onFailure = { error ->
                    mutableState.emit(
                        TableDetailsScreenState(isLoading = false, error = error)
                    )
                }
            )
        }
    }

    fun openTable(table: Table) {

    }

    fun closeTable(table: Table) {

    }

    fun makeOrder() {

    }
}
