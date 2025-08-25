package co.kandalabs.comandaai.data.repository

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.data.api.CommanderApi
import co.kandalabs.comandaai.domain.repository.ItemsRepository
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemStatus
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