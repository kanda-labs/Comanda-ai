package domain.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.ItemUnitStatus
import kandalabs.commander.domain.model.KitchenItemDetail
import kandalabs.commander.domain.model.KitchenOrder
import kandalabs.commander.domain.repository.OrderRepository
import kandalabs.commander.domain.service.KitchenService
import kandalabs.commander.domain.service.KitchenServiceImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KitchenServiceTest {
    
    private lateinit var orderRepository: OrderRepository
    private lateinit var kitchenService: KitchenService
    
    @BeforeEach
    fun setup() {
        orderRepository = mockk()
        kitchenService = KitchenServiceImpl(orderRepository)
    }
    
    @Test
    fun `should return only orders with incomplete items`() = runTest {
        // Given
        val completeOrder = KitchenOrder(
            id = 1,
            tableNumber = 1,
            userName = "test-user",
            items = listOf(
                KitchenItemDetail(
                    itemId = 1,
                    name = "Test Item",
                    totalCount = 2,
                    observation = null,
                    unitStatuses = listOf(
                        ItemUnitStatus(0, ItemStatus.DELIVERED, System.currentTimeMillis(), "user1"),
                        ItemUnitStatus(1, ItemStatus.DELIVERED, System.currentTimeMillis(), "user1")
                    ),
                    overallStatus = ItemStatus.DELIVERED
                )
            ),
            createdAt = System.currentTimeMillis()
        )
        
        val incompleteOrder = KitchenOrder(
            id = 2,
            tableNumber = 2,
            userName = "test-user2",
            items = listOf(
                KitchenItemDetail(
                    itemId = 2,
                    name = "Test Item 2",
                    totalCount = 2,
                    observation = null,
                    unitStatuses = listOf(
                        ItemUnitStatus(0, ItemStatus.DELIVERED, System.currentTimeMillis(), "user1"),
                        ItemUnitStatus(1, ItemStatus.OPEN, System.currentTimeMillis(), "user1")
                    ),
                    overallStatus = ItemStatus.OPEN
                )
            ),
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { orderRepository.getOrdersWithIncompleteItems() } returns Result.success(
            listOf(incompleteOrder)
        )
        
        // When
        val result = kitchenService.getActiveOrdersForKitchen()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(incompleteOrder, result.getOrNull()?.first())
        
        coVerify { orderRepository.getOrdersWithIncompleteItems() }
    }
    
    @Test
    fun `should update individual item unit status with valid kitchen status`() = runTest {
        // Given
        val orderId = 1
        val itemId = 1
        val unitIndex = 0
        val newStatus = ItemStatus.IN_PRODUCTION
        val updatedBy = "kitchen_user"
        
        coEvery { 
            orderRepository.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus, updatedBy) 
        } returns Result.success(true)
        
        // When
        val result = kitchenService.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus, updatedBy)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        
        coVerify { 
            orderRepository.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus, updatedBy) 
        }
    }
    
    @Test
    fun `should reject update with invalid kitchen status`() = runTest {
        // Given
        val orderId = 1
        val itemId = 1
        val unitIndex = 0
        val invalidStatus = ItemStatus.GRANTED // Legacy status not used in kitchen
        val updatedBy = "kitchen_user"
        
        // When
        val result = kitchenService.updateItemUnitStatus(orderId, itemId, unitIndex, invalidStatus, updatedBy)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Invalid status for kitchen operations: GRANTED", result.exceptionOrNull()?.message)
        
        // Should not call repository for invalid status
        coVerify(exactly = 0) { 
            orderRepository.updateItemUnitStatus(any(), any(), any(), any(), any()) 
        }
    }
    
    @Test
    fun `should handle repository failure gracefully`() = runTest {
        // Given
        val expectedError = RuntimeException("Database connection failed")
        coEvery { orderRepository.getOrdersWithIncompleteItems() } returns Result.failure(expectedError)
        
        // When
        val result = kitchenService.getActiveOrdersForKitchen()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }
    
    @Test
    fun `should validate kitchen status correctly`() {
        // Valid kitchen statuses
        val validStatuses = listOf(
            ItemStatus.OPEN,
            ItemStatus.IN_PRODUCTION,
            ItemStatus.COMPLETED,
            ItemStatus.DELIVERED,
            ItemStatus.CANCELED
        )
        
        validStatuses.forEach { status ->
            val service = KitchenServiceImpl(orderRepository)
            // Use reflection to access private method for testing
            val method = service::class.java.getDeclaredMethod("isValidKitchenStatus", ItemStatus::class.java)
            method.isAccessible = true
            assertTrue(method.invoke(service, status) as Boolean, "Status $status should be valid for kitchen")
        }
        
        // Invalid kitchen status
        val service = KitchenServiceImpl(orderRepository)
        val method = service::class.java.getDeclaredMethod("isValidKitchenStatus", ItemStatus::class.java)
        method.isAccessible = true
        assertFalse(method.invoke(service, ItemStatus.GRANTED) as Boolean, "GRANTED status should not be valid for kitchen")
    }
}