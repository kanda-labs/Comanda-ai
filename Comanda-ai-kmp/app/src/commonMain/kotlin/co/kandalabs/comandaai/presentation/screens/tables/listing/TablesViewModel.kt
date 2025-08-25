package co.kandalabs.comandaai.presentation.screens.tables.listing

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.LogoutManager
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.domain.repository.TablesRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

internal class TablesViewModel(
    private val repository: TablesRepository,
    private val sessionManager: SessionManager
) : StateScreenModel<TablesScreenState>(TablesScreenState()) {

    fun retrieveTables() {
        screenModelScope.launch {
            mutableState.emit(TablesScreenState(isLoading = true))


            repository.getTables()
                .fold(
                    onSuccess = { tables ->
                        mutableState.emit(
                            TablesScreenState(
                                tables = tables.toPersistentList(),
                                isLoading = false,
                                error = null
                            )
                        )
                    },
                    onFailure = { error ->
                        mutableState.emit(
                            TablesScreenState(isLoading = false, error = error)
                        )
                    }
                )
        }
    }
    
    fun logout() {
        // Use LogoutManager to prevent "Parent job is Completed" errors
        LogoutManager.performLogout(sessionManager)
    }
    
    suspend fun getUserSession() = sessionManager.getSession()
}
