package co.kandalabs.comandaai.kitchen.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.session.UserSession
import co.kandalabs.comandaai.core.enums.UserRole
import co.kandalabs.comandaai.kitchen.data.api.KitchenEvent
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.ItemUnitStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenItemDetail
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KitchenViewModelTest {
    
    private lateinit var repository: FakeKitchenRepository
    private lateinit var sessionManager: FakeSessionManager
    private lateinit var viewModel: KitchenViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeKitchenRepository()
        sessionManager = FakeSessionManager()
        viewModel = KitchenViewModel(repository, sessionManager)
    }
    
    @Test
    fun `should load active orders on init`() = runTest {
        // Given
        val expectedOrders = listOf(
            KitchenOrder(
                id = 1,
                tableNumber = 1,
                items = listOf(
                    KitchenItemDetail(
                        itemId = 1,
                        name = "Test Item",
                        totalCount = 2,
                        observation = null,
                        unitStatuses = listOf(
                            ItemUnitStatus(0, ItemStatus.OPEN, 1234567890L, null),
                            ItemUnitStatus(1, ItemStatus.OPEN, 1234567890L, null)
                        ),
                        overallStatus = ItemStatus.OPEN
                    )
                ),
                createdAt = 1234567890L
            )
        )
        repository.setOrders(expectedOrders)
        
        // When/Then
        viewModel.state.test {
            // Initial state should be loading
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            assertThat(initialState.orders).isEqualTo(emptyList())
            assertThat(initialState.error).isNull()
            
            // After loading, should have orders
            testDispatcher.scheduler.advanceUntilIdle()
            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            assertThat(loadedState.orders).isEqualTo(expectedOrders)
            assertThat(loadedState.error).isNull()
        }
    }
    
    @Test
    fun `should handle loading error`() = runTest {
        // Given
        val errorMessage = "Network error"
        repository.setError(RuntimeException(errorMessage))
        
        // When/Then
        viewModel.state.test {
            // Initial loading state
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            
            // Error state
            testDispatcher.scheduler.advanceUntilIdle()
            val errorState = awaitItem()
            assertThat(errorState.isLoading).isFalse()
            assertThat(errorState.error).isEqualTo(errorMessage)
            assertThat(errorState.orders).isEqualTo(emptyList())
        }
    }
    
    @Test
    fun `should update item status and refresh list`() = runTest {
        // Given
        val initialOrders = listOf(
            KitchenOrder(
                id = 1,
                tableNumber = 1,
                items = listOf(
                    KitchenItemDetail(
                        itemId = 1,
                        name = "Test Item",
                        totalCount = 1,
                        observation = null,
                        unitStatuses = listOf(
                            ItemUnitStatus(0, ItemStatus.OPEN, 1234567890L, null)
                        ),
                        overallStatus = ItemStatus.OPEN
                    )
                ),
                createdAt = 1234567890L
            )
        )
        
        val updatedOrders = listOf(
            initialOrders[0].copy(
                items = listOf(
                    initialOrders[0].items[0].copy(
                        unitStatuses = listOf(
                            ItemUnitStatus(0, ItemStatus.IN_PRODUCTION, 1234567890L, "kitchen_user")
                        ),
                        overallStatus = ItemStatus.IN_PRODUCTION
                    )
                )
            )
        )
        
        repository.setOrders(initialOrders)
        
        viewModel.state.test {
            // Skip initial states
            awaitItem() // loading
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loaded initial orders
            
            // When - update item status
            repository.setOrders(updatedOrders)
            viewModel.updateItemStatus(1, 1, 0, ItemStatus.IN_PRODUCTION)
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Then - should have updated orders
            val updatedState = awaitItem()
            assertThat(updatedState.orders).isEqualTo(updatedOrders)
            assertThat(updatedState.error).isNull()
        }
    }
    
    @Test
    fun `should handle update item status error`() = runTest {
        // Given
        repository.setOrders(emptyList())
        repository.setUpdateError(RuntimeException("Update failed"))
        
        viewModel.state.test {
            // Skip initial states
            awaitItem() // loading
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loaded empty orders
            
            // When - update item status fails
            viewModel.updateItemStatus(1, 1, 0, ItemStatus.IN_PRODUCTION)
            testDispatcher.scheduler.advanceUntilIdle()
            
            // Then - should show error
            val errorState = awaitItem()
            assertThat(errorState.error).isEqualTo("Update failed")
        }
    }
    
    @Test
    fun `should refresh orders`() = runTest {
        // Given
        val orders = listOf(
            KitchenOrder(
                id = 1,
                tableNumber = 1,
                items = emptyList(),
                createdAt = 1234567890L
            )
        )
        repository.setOrders(orders)
        
        viewModel.state.test {
            // Skip initial states
            awaitItem() // loading
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // loaded orders
            
            // When - refresh
            viewModel.refreshOrders()
            
            // Then - should show refreshing state
            val refreshingState = awaitItem()
            assertThat(refreshingState.isRefreshing).isTrue()
            
            testDispatcher.scheduler.advanceUntilIdle()
            val refreshedState = awaitItem()
            assertThat(refreshedState.isRefreshing).isFalse()
            assertThat(refreshedState.orders).isEqualTo(orders)
        }
    }
    
    @Test
    fun `should clear error`() = runTest {
        // Given
        repository.setError(RuntimeException("Test error"))
        
        viewModel.state.test {
            // Skip to error state
            awaitItem() // loading
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // error state
            
            // When - clear error
            viewModel.clearError()
            
            // Then - error should be null
            val clearedState = awaitItem()
            assertThat(clearedState.error).isNull()
        }
    }
}

// Fake repository for testing
private class FakeKitchenRepository : KitchenRepository {
    private var orders: List<KitchenOrder> = emptyList()
    private var getOrdersError: Exception? = null
    private var updateError: Exception? = null
    
    fun setOrders(orders: List<KitchenOrder>) {
        this.orders = orders
        this.getOrdersError = null
    }
    
    fun setError(error: Exception) {
        this.getOrdersError = error
    }
    
    fun setUpdateError(error: Exception) {
        this.updateError = error
    }
    
    override suspend fun getActiveOrders(): Result<List<KitchenOrder>> {
        return if (getOrdersError != null) {
            Result.failure(getOrdersError!!)
        } else {
            Result.success(orders)
        }
    }
    
    override suspend fun updateItemUnitStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        status: ItemStatus
    ): Result<Unit> {
        return if (updateError != null) {
            Result.failure(updateError!!)
        } else {
            Result.success(Unit)
        }
    }
    
    override suspend fun markOrderAsDelivered(orderId: Int): Result<Unit> {
        return if (updateError != null) {
            Result.failure(updateError!!)
        } else {
            Result.success(Unit)
        }
    }
    
    override suspend fun markItemAsDelivered(orderId: Int, itemId: Int): Result<Unit> {
        return if (updateError != null) {
            Result.failure(updateError!!)
        } else {
            Result.success(Unit)
        }
    }
    
    override fun getOrdersRealTime(): Flow<KitchenEvent> {
        // Para testes, retorna um fluxo vazio
        return emptyFlow()
    }
}

// Fake session manager for testing
private class FakeSessionManager : SessionManager {
    private var session: UserSession? = UserSession(
        userId = 1,
        userName = "test",
        email = "test@example.com",
        role = UserRole.ADMIN,
        token = "test-token"
    )
    
    override suspend fun saveSession(session: UserSession) {
        this.session = session
    }
    
    override suspend fun getSession(): UserSession? {
        return session
    }
    
    override suspend fun clearSession() {
        session = null
    }
    
    override suspend fun logout() {
        session = null
    }
    
    override suspend fun hasActiveSession(): Boolean {
        return session != null
    }
}