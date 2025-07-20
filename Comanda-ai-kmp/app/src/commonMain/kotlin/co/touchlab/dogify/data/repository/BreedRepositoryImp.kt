package co.touchlab.dogify.data.repository

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.coroutinesResult.safeRunCatching
import co.touchlab.dogify.core.logger.DogifyLogger
import co.touchlab.dogify.data.api.CommanderApi
import co.touchlab.dogify.domain.repository.ItemsRepository
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ItemsRepositoryImp(
    val api: CommanderApi,
    val logger: DogifyLogger,
    val dispatcher: CoroutineDispatcher,
) : ItemsRepository {
    override suspend fun getItems(nameStatus: ItemStatus?): DogifyResult<List<Item>> =
        withContext(dispatcher) {
            safeRunCatching {
               api.getItems()
            }.onFailure { error ->
                logger.e(error, "Error retrieving breeds -> ${error.message}")
            }
        }
}