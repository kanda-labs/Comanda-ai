package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.migration

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TableMigrationState(
    val isLoading: Boolean = false,
    val freeTables: List<Table> = emptyList(),
    val error: String? = null,
    val isMigrating: Boolean = false
)

class TableMigrationViewModel(
    private val tablesRepository: TablesRepository
) : ScreenModel {
    
    private val _state = MutableStateFlow(TableMigrationState())
    val state: StateFlow<TableMigrationState> = _state.asStateFlow()

    fun loadTables() {
        screenModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            tablesRepository.getFreeTables()
                .fold(
                    onSuccess = { freeTables ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            freeTables = freeTables
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Erro ao carregar mesas livres: ${error.message}"
                        )
                    }
                )
        }
    }

    fun migrateTable(
        originTable: Table,
        destinationTable: Table,
        onSuccess: (Pair<Table, Table>) -> Unit
    ) {
        screenModelScope.launch {
            _state.value = _state.value.copy(isMigrating = true, error = null)
            
            val originId = originTable.id ?: return@launch
            val destinationId = destinationTable.id ?: return@launch
            
            tablesRepository.migrateTable(originId, destinationId)
                .fold(
                    onSuccess = { (updatedOrigin, updatedDestination) ->
                        _state.value = _state.value.copy(isMigrating = false)
                        onSuccess(Pair(updatedOrigin, updatedDestination))
                    },
                    onFailure = { error ->
                        val errorMessage = when {
                            error.message?.contains("not free") == true -> {
                                "A mesa ${destinationTable.number} foi ocupada. Selecione uma nova mesa."
                            }
                            error.message?.contains("not occupied") == true -> {
                                "A mesa ${originTable.number} não está ocupada."
                            }
                            error.message?.contains("500") == true -> {
                                "Erro interno do servidor. Tente novamente."
                            }
                            error.message?.contains("404") == true -> {
                                "Mesa não encontrada. Recarregue a lista."
                            }
                            else -> "Erro ao migrar mesa: ${error.message}"
                        }
                        _state.value = _state.value.copy(
                            isMigrating = false,
                            error = errorMessage
                        )
                        // Refresh free tables to show current state
                        loadTables()
                    }
                )
        }
    }
}