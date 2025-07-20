@file:OptIn(ExperimentalCoroutinesApi::class)

package co.touchlab.dogify.domain.usecases

import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.error.ComandaAiException
import co.touchlab.dogify.domain.models.Page
import co.touchlab.dogify.domain.models.PageConfig
import co.touchlab.dogify.fakes.FakeItemsRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class BreedsPaginationUseCaseImplTest {

    private val pageSize = 10
    private lateinit var repository: FakeItemsRepository
    private lateinit var useCase: BreedsPaginationUseCaseImpl

    @BeforeTest
    fun setup() {
        repository = FakeItemsRepository()
        useCase = BreedsPaginationUseCaseImpl(
            repository = repository,
            pageSize = pageSize
        )
        Dispatchers.resetMain()
    }

    @Test
    fun `given getNextPage is called first time then the use case should return first page properly`() =
        runTest {
            val totalItems = 20
            val expectedPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 10, size = pageSize, totalItems = totalItems),
                    items = persistentListOf(
                        Breed(id = 1, name = "Breed1", subBreedName = "SubBreed"),
                        Breed(id = 2, name = "Breed2")
                    )
                )
            )
            repository.pagesForOffset[0] = expectedPage

            val result = useCase.getNextPage()

            assertEquals(expectedPage, result)
            assertEquals(1, repository.callCount)
        }

    @Test
    fun `given getNextPage is called multiple times then use case should return every page properly`() =
        runTest {
            val totalBreeds = 50
            val firstExpectedPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 10, size = pageSize, totalItems = totalBreeds),
                    items = List(10) {
                        Breed(id = it.toLong(), name = "Breed$it")
                    }.toPersistentList()
                )
            )
            val secondExpectedPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 20, size = pageSize, totalItems = totalBreeds),
                    items = List(10) {
                        Breed(
                            id = it.toLong(),
                            name = "Breed ${it + 10}"
                        )
                    }.toPersistentList()
                )
            )
            repository.pagesForOffset[0] = firstExpectedPage
            repository.pagesForOffset[10] = secondExpectedPage

            val firstPageResult = useCase.getNextPage()
            val secondPageResult = useCase.getNextPage()

            assertEquals(2, repository.callCount)
            assertEquals(firstExpectedPage, firstPageResult)
            assertEquals(secondExpectedPage, secondPageResult)
        }

    @Test
    fun `given getNextPage is called when there is not more data then returns empty page and not call repository`() =
        runTest {
            val firstExpectedPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 0, size = pageSize, totalItems = 2),
                    items = persistentListOf(Breed(id = 1, "Breed1"), Breed(id = 2, "Breed2"))
                )
            )
            val lastExpectedPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 2, size = pageSize, totalItems = 2),
                    items = persistentListOf<Breed>()
                )
            )
            repository.pagesForOffset[0] = firstExpectedPage
            repository.pagesForOffset[2] = lastExpectedPage

            val firstPageResult = useCase.getNextPage()
            val lastPageResult = useCase.getNextPage()

            assertEquals(1, repository.callCount)
            assertEquals(firstExpectedPage, firstPageResult)
            assertEquals(lastExpectedPage, lastPageResult)

            val emptyPageResult = useCase.getNextPage()

            assertEquals(1, repository.callCount)
            emptyPageResult.onSuccess {
                assertTrue(it.items.isEmpty())
            }
        }

    @Test
    fun `given getNextPage is called when fetch more pages than total items then pageConfig offset is equal to pageConfig totalItems`() =
        runTest {
            val page1 = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 0, size = pageSize, totalItems = 2),
                    items = persistentListOf(Breed(id = 1, "Breed1"), Breed(id = 2, "Breed2"))
                )
            )
            val page2 = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 2, size = pageSize, totalItems = 2),
                    items = persistentListOf<Breed>()
                )
            )
            repository.pagesForOffset[0] = page1
            repository.pagesForOffset[2] = page2

            useCase.getNextPage()
            val secondPageResult = useCase.getNextPage()
            secondPageResult.onSuccess {
                assertTrue(it.pageConfig.offset == 2)
                assertTrue(it.pageConfig.size == 10)
                assertTrue(it.pageConfig.totalItems == 2)
            }
        }


    @Test
    fun `given getNextPage fails in first call when getNextPage is called again repository would be called too`() =
        runTest {
            repository.error = ComandaAiException.NoInternetConnectionException

            val retryFirstPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 2, size = pageSize, totalItems = 2),
                    items = persistentListOf<Breed>()
                )
            )
            repository.pagesForOffset[0] = retryFirstPage

            val firstPageResult = useCase.getNextPage()

            repository.error = null

            val secondAttemptResult = useCase.getNextPage()

            assertTrue { firstPageResult is DogifyResult.Failure }
            assertTrue { secondAttemptResult is DogifyResult.Success }
            assertTrue { repository.callCount == 2 }
            secondAttemptResult.onSuccess {
                assertTrue(it.pageConfig.offset == 2)
                assertTrue(it.pageConfig.size == 10)
                assertTrue(it.pageConfig.totalItems == 2)
            }
        }
}