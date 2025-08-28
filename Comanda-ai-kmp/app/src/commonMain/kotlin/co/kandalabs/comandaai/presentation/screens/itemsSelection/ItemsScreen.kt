package co.kandalabs.comandaai.presentation.screens.itemsSelection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.ErrorView
import comandaai.app.generated.resources.Res
import comandaai.app.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource
import coil3.compose.AsyncImage
import co.kandalabs.comandaai.domain.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview

private const val PREVIEW_BACKGROUND_ALPHA = 0.7f
private const val BREED_ITEM_BACKGROUND_ALPHA = 0.45f
private const val PREVIEW_CARD_MAX_HEIGHT_RATIO = 0.5f
private const val PREVIEW_CARD_MAX_WIDTH_RATIO = 0.8f
private const val SQUARE_RATIO = 1f
private const val LISTING_LAZY_GRID_TEST_TAG = "ListingLazyGrid"
private const val COLUMNS_COUNT = 2
private const val LISTING_LOADING_VIEW_TEST_TAG = "ListingLoadingView"
private val CIRCULAR_PROGRESS_INDICATOR_SIZE = 24.dp

internal class ItemsScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel<BreedsListingViewModel>()
        val state = screenModel.state.collectAsState().value
        val fetchMoreItems = {
            screenModel.retrieveItems()
        }
        val onRetry = {
            screenModel.retrieveItems()
        }

        BreedsListingScreenContent(
            state = state,
            fetchMoreItems = fetchMoreItems,
            retry = onRetry
        )

        LaunchedEffect(Unit) {
            screenModel.retrieveItems()
        }
    }
}


@Composable
internal fun BreedsListingScreenContent(
    state: ItemsSelectionScreenState,
    fetchMoreItems: () -> Unit,
    retry: () -> Unit
) {
    val strings = ItemsSelectionStrings()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ComandaAiTopAppBar(strings.title)
        when {
            state.isLoading -> ComandaAiLoadingView(
                loadingImage = painterResource(Res.drawable.golden_loading),
                testTag = LISTING_LOADING_VIEW_TEST_TAG
            )
            state.error != null -> {
                ErrorView(error = state.error, retry)
            }

            else -> {
                GridList(
                    items = state.Items,
                    fetchMoreItems = fetchMoreItems
                )
            }
        }
    }
}


@Composable
private fun GridList(
    items: ImmutableList<Item>,
    fetchMoreItems: () -> Unit
) {
    val lazyGridState = rememberLazyListState()

    var previewUrl by remember { mutableStateOf<String?>(null) }
    var isPreviewVisible by remember { mutableStateOf(false) }

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (lastVisibleItem >= totalItems - 2) {
                    fetchMoreItems()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(LISTING_LAZY_GRID_TEST_TAG)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(ComandaAiSpacing.xSmall.value),
            state = lazyGridState
        ) {
            items(items) { item ->
                BreedItem(
                    item = item,
                )
            }
        }

        if (isPreviewVisible && !previewUrl.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = PREVIEW_BACKGROUND_ALPHA)),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    shape = RoundedCornerShape(ComandaAiSpacing.Medium.value),
                    modifier = Modifier
                        .fillMaxHeight(PREVIEW_CARD_MAX_HEIGHT_RATIO)
                        .fillMaxWidth(PREVIEW_CARD_MAX_WIDTH_RATIO)
                        .aspectRatio(1f)
                ) {
                    AsyncImage(
                        model = previewUrl,
                        contentDescription = "Preview Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun BreedItem(
    item: Item,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = BREED_ITEM_BACKGROUND_ALPHA))
            .padding(ComandaAiSpacing.xXSmall.value)
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Preview
@Composable
private fun BreedsListingScreenPreview() {
    MaterialTheme {
        BreedsListingScreenContent(
            state = ItemsSelectionScreenState(
                Items = persistentListOf(
                    Item(
                        id = 1,
                        name = "Espetinho de Frango",
                        value = 1200,
                        category = co.kandalabs.comandaai.domain.ItemCategory.SKEWER,
                        description = "Espetinho de frango suculento"
                    ),
                    Item(
                        id = 2,
                        name = "Cerveja",
                        value = 800,
                        category = co.kandalabs.comandaai.domain.ItemCategory.DRINK,
                        description = "Cerveja gelada"
                    ),
                    Item(
                        id = 3,
                        name = "Refrigerante",
                        value = 600,
                        category = co.kandalabs.comandaai.domain.ItemCategory.DRINK,
                        description = "Refrigerante lata"
                    ),
                    Item(
                        id = 4,
                        name = "Chopp",
                        value = 1000,
                        category = co.kandalabs.comandaai.domain.ItemCategory.DRINK,
                        description = "Chopp artesanal"
                    )
                ),
                isLoading = false,
                error = null
            ),
            fetchMoreItems = { },
            retry = { }
        )
    }
}
