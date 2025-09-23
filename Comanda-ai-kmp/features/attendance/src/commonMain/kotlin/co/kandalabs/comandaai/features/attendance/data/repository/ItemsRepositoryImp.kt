package co.kandalabs.comandaai.features.attendance.data.repository

import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.features.attendance.data.api.CommanderApi
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.sdk.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.sdk.coroutinesResult.safeRunCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ItemsRepositoryImp(
    val api: CommanderApi,
    val logger: ComandaAiLogger,
    val dispatcher: CoroutineDispatcher,
) : ItemsRepository {
    override suspend fun getItems(nameStatus: ItemStatus?): ComandaAiResult<List<Item>> =
        withContext(dispatcher) {
            safeRunCatching {
               api.getItems()
            }.onFailure { error ->
                logger.e(error, "Error retrieving breeds -> ${error.message}")
            }
        }
}