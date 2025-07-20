@file:OptIn(ExperimentalCoroutinesApi::class)

package co.touchlab.dogify.data.repository

import FakeCommanderApi
import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.data.models.BreedsAndSubBreedsResponse
import co.touchlab.dogify.domain.models.PageConfig
import co.touchlab.dogify.fakes.FakeDogifyLogger
import co.touchlab.dogify.fakes.FakeLocalBreedRepository
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlin.test.assertFalse

private typealias GenericError = Throwable

class BreedsRepositoryImpTest {
    private val testScheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeApi: FakeCommanderApi
    private lateinit var fakeLogger: FakeDogifyLogger
    private lateinit var repository: ItemsRepositoryImp
    private lateinit var localRepository: FakeLocalBreedRepository


    @BeforeTest
    fun setUp() {
        fakeApi = FakeCommanderApi()
        fakeLogger = FakeDogifyLogger()
        this@BreedsRepositoryImpTest.localRepository = FakeLocalBreedRepository()
        repository = ItemsRepositoryImp(
            api = fakeApi,
            logger = fakeLogger,
            dispatcher = dispatcher,
            localRepository = this@BreedsRepositoryImpTest.localRepository
        )

        Dispatchers.resetMain()
    }

    @Test
    fun `given getBreedsPage is called when localBreeds is empty then the API is called to populate localBreeds`() {
        val breedsAndSubBreedsResponse = BreedsAndSubBreedsResponse(
            status = "success",
            breeds = mapOf(
                "breed1" to listOf("firstSubBreed1", "secondSubBreed1"),
                "breed2" to listOf("firstSubBreed2", "secondSubBreed2"),
            )
        )

        return runTest(dispatcher) {
            val pageConfig = PageConfig(offset = 0, size = 10)
            assertTrue(repository.localRepository.isEmpty())
            fakeApi.response = breedsAndSubBreedsResponse

            val newPageResult = repository.getBreedsPage(pageConfig)
            testScheduler.advanceUntilIdle()

            assertEquals(1, fakeApi.callBreedsAndSubBreedsCount)
            assertFalse(repository.localRepository.isEmpty())
            assertEquals(4, localRepository.localBreeds.size)
            newPageResult.onSuccess { assertEquals(4, it.items.size) }
        }
    }

    @Test
    fun `given getBreedsPage is called when localBreeds is not empty, should not call the API`() =
        runTest(dispatcher) {
            val storedBreeds = listOf(
                Breed(1, "Breed1"),
                Breed(2, "Breed2")
            ).toPersistentList()
            val pageConfig = PageConfig(offset = 0, size = 10)

            localRepository.localBreeds = storedBreeds

            val newPageResult = repository.getBreedsPage(pageConfig)

            assertEquals(0, fakeApi.callBreedsAndSubBreedsCount)
            newPageResult.onSuccess { assertEquals(2, it.items.size) }
        }

    @Test
    fun `given getBreedsPage is called when the API fails then it should log the error`() =
        runTest(dispatcher) {
            fakeApi.shouldThrow = RuntimeException("Simulated API error")

            val pageConfig = PageConfig(offset = 0, size = 10)

            val newPageResult = repository.getBreedsPage(pageConfig)


            assertTrue(fakeLogger.errorMessages.isNotEmpty())
            assertTrue(fakeLogger.errorMessages[0].contains("Simulated API error"))
            assertTrue { newPageResult is DogifyResult.Failure }

        }

    @Test
    fun `given getBreedsPage is called when offset is larger than the list size then return an empty sublist`() =
        runTest(dispatcher) {
            localRepository.localBreeds = listOf(
                Breed(1, "Breed1"),
                Breed(2, "Breed2")
            ).toPersistentList()

            val pageConfig = PageConfig(offset = 10, size = 10, totalItems = 2)
            val resultPage = repository.getBreedsPage(pageConfig)

            resultPage.onSuccess {
                assertTrue(it.items.isEmpty())
            }
        }

    @Test
    fun `given getBreedsPage is called when page config total items is bigger then zero then count() should not be called`() =
        runTest(dispatcher) {
            localRepository.localBreeds = listOf(
                Breed(1, "Breed1"),
                Breed(2, "Breed2")
            ).toPersistentList()
            val pageConfig = PageConfig(offset = 0, size = 10, totalItems = 2)
            val resultPage = repository.getBreedsPage(pageConfig)
            resultPage.onSuccess {
                assertEquals(0, localRepository.countSpy)
            }
        }

    @Test
    fun `given getBreedsPage is called when fetchImage fails then it should not throw the error`() =
        runTest(dispatcher) {
            fakeApi.shouldThrowOnImage = RuntimeException("Simulated API error")
            localRepository.localBreeds = listOf(
                Breed(1, "Breed1"),
                Breed(2, "Breed2")
            ).toPersistentList()

            val pageConfig = PageConfig(offset = 0, size = 10)
            val resultPage = repository.getBreedsPage(pageConfig)
            resultPage.onSuccess {
                assertEquals(2, it.items.size)
                assertEquals(it.items[0].url, null)
                assertEquals(it.items[1].url, null)
            }
        }
}
