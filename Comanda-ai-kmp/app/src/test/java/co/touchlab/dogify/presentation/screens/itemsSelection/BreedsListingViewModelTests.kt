@file:OptIn(ExperimentalCoroutinesApi::class)

package co.touchlab.dogify.presentation.screens.itemsSelection

import app.cash.turbine.test
import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.core.error.ComandaAiException
import co.touchlab.dogify.domain.models.Page
import co.touchlab.dogify.domain.models.PageConfig
import co.touchlab.dogify.domain.usecases.BreedsPaginationUseCase
import co.touchlab.dogify.domain.usecases.BreedsPaginationUseCaseImpl
import co.touchlab.dogify.fakes.FakeItemsRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BreedsListingViewModelTests {
    private val testScheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var viewModel: BreedsListingViewModel
    private lateinit var useCase: BreedsPaginationUseCase
    private lateinit var repository: FakeItemsRepository

    @BeforeTest
    fun setup() {
        repository = FakeItemsRepository()
        useCase = BreedsPaginationUseCaseImpl(repository)
        viewModel = BreedsListingViewModel(
            paginationUseCase = useCase
        )

        Dispatchers.resetMain()
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun `given retrieve is called first time then Loading state should be settled properly`() =
        runTest {
            viewModel.retrieveBreeds()
            viewModel.state.test {
                val result = awaitItem()
                assertEquals(result.Items.isEmpty(), true)
                assertEquals(result.isLoading, true)
                assertEquals(result.error, null)
            }
        }

    @Test
    fun `given retrieve is called first time then first page should be loaded properly`() =
        runTest {
            val totalItems = 2
            val expectedPage = DogifyResult.Success(
                Page(
                    pageConfig = PageConfig(offset = 0, totalItems = totalItems),
                    items = persistentListOf(
                        Breed(id = 1, name = "Breed1"),
                        Breed(id = 2, name = "Breed2")
                    )
                )
            )
            repository.pagesForOffset[0] = expectedPage

            viewModel.retrieveBreeds()
            advanceUntilIdle()

            viewModel.state.test {
                val result = expectMostRecentItem()
                assertTrue(result.Items.size == 2)
                assertFalse(result.isLoading)
                assertTrue(result.error == null)
            }
        }

    @Test
    fun `given retrieveBreeds is called when an error occurs then error state should be settled properly`() =
        runTest {
            repository.error = ComandaAiException.UnknownException("Internal Server Error")

            viewModel.retrieveBreeds()
            advanceUntilIdle()

            viewModel.state.test {
                val result = expectMostRecentItem()
                assertTrue(result.Items.isEmpty())
                assertFalse(result.isLoading)
                assertTrue(result.error is ComandaAiException.UnknownException)

            }
        }
}