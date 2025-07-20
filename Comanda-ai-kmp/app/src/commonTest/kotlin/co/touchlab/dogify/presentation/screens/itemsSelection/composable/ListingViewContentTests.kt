@file:OptIn(ExperimentalTestApi::class)

package co.touchlab.dogify.presentation.screens.itemsSelection.composable

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import co.touchlab.dogify.core.error.ComandaAiException
import co.touchlab.dogify.presentation.designSystem.theme.ComandaAiTheme
import co.touchlab.dogify.presentation.screens.itemsSelection.BreedsListingScreenContent
import co.touchlab.dogify.presentation.screens.itemsSelection.ItemsSelectionScreenState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.test.Test
import kotlin.test.assertEquals

class ListingViewContentTests {

    @Test()
    fun testLoadingState() = runComposeUiTest {
        setContent {
            ComandaAiTheme {
                BreedsListingScreenContent(
                    state = ItemsSelectionScreenState(
                        isLoading = true,
                        Items = persistentListOf(),
                        error = null
                    ),
                    fetchMoreItems = {},
                    retry = {}
                )
            }
        }

        onNodeWithTag("ListingLoadingView").assertIsDisplayed()
    }

    @Test
    fun testErrorState() = runComposeUiTest {
        val comandaAiException = ComandaAiException.NoInternetConnectionException

        setContent {
            ComandaAiTheme {
                BreedsListingScreenContent(
                    state = ItemsSelectionScreenState(
                        isLoading = false,
                        Items = persistentListOf(),
                        error = comandaAiException
                    ),
                    fetchMoreItems = {},
                    retry = {}
                )
            }
        }

        onNodeWithText("Ops! something went wrong").assertIsDisplayed()
        onNodeWithText("code: 10000").assertIsDisplayed()
        onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun testBreedNamesDisplayed() = runComposeUiTest {
        setContent {
            ComandaAiTheme {
                BreedsListingScreenContent(
                    state = ItemsSelectionScreenState(
                        isLoading = false,
                        Items = persistentListOf(
                            Breed(id = 1, name = "Labrador", subBreedName = "Retriever"),
                            Breed(id = 2, name = "Golden", subBreedName = "Retriever"),
                            Breed(id = 3, name = "French", subBreedName = "Bulldog")
                        ),
                        error = null
                    ),
                    fetchMoreItems = {},
                    retry = {}
                )
            }
        }

        onNodeWithText("Retriever Labrador").assertIsDisplayed()
        onNodeWithText("Retriever Golden").assertIsDisplayed()
        onNodeWithText("Bulldog French").assertIsDisplayed()
        onNodeWithText("Dog Breeds").assertIsDisplayed()
    }

    @Test
    fun testFetchMoreItemsIsCalledOnScrollEnd() = runComposeUiTest {
        var timesCalled = 0
        val mockFetchMore: () -> Unit = {
            timesCalled++
        }

        val breeds = List(30) {
            Breed(
                id = it.toLong(),
                name = "Labrador",
                subBreedName = "Retriever$it",
                url = "https://images.dog.ceo/breeds/affenpinscher/n02110627_11819.jpg"
            )
        }.toPersistentList()

        setContent {
            ComandaAiTheme {
                BreedsListingScreenContent(
                    state = ItemsSelectionScreenState(
                        isLoading = false,
                        Items = breeds,
                        error = null
                    ),
                    fetchMoreItems = mockFetchMore,
                    retry = {}
                )
            }
        }

        onNodeWithTag("ListingLazyGrid").assertExists()

        waitForIdle()
        assertEquals(1, timesCalled)
    }
}