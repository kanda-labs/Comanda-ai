package co.touchlab.dogify.fakes

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.error.ComandaAiException
import co.touchlab.dogify.domain.models.Page
import co.touchlab.dogify.domain.models.PageConfig
import co.touchlab.dogify.domain.repository.ItemsRepository

internal class FakeItemsRepository : ItemsRepository {
    val pagesForOffset = mutableMapOf<Int, DogifyResult<Page<Breed>>>()
    var error: ComandaAiException? = null
    var callCount = 0
        private set

    override suspend fun getBreedsPage(page: PageConfig): DogifyResult<Page<Breed>> {
        callCount++
        return when {
            error != null -> DogifyResult.Failure(error!!)
            else -> pagesForOffset[page.offset] ?: DogifyResult.Success(Page())
        }
    }
}