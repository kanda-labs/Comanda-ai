package co.kandalabs.comandaai.kitchen.data.api

import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.domain.model.KitchenItemDetail
import co.kandalabs.comandaai.kitchen.domain.model.ItemUnitStatus
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KitchenSSESerializationTest {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun testKitchenOrdersUpdateEventDeserialization() {
        val jsonString = """
        {
            "type": "kitchen_orders_update",
            "orders": [
                {
                    "id": 29,
                    "tableNumber": 1,
                    "userName": "test-user",
                    "items": [
                        {
                            "itemId": 1,
                            "name": "Espetinho de Alcatra",
                            "totalCount": 1,
                            "observation": null,
                            "unitStatuses": [
                                {
                                    "unitIndex": 0,
                                    "status": "OPEN",
                                    "updatedAt": 1755707429754,
                                    "updatedBy": null
                                }
                            ],
                            "overallStatus": "OPEN",
                            "category": "SKEWER"
                        }
                    ],
                    "createdAt": 1755707273395
                }
            ],
            "timestamp": 1755707429754
        }
        """.trimIndent()

        val event = json.decodeFromString<KitchenOrdersUpdateEvent>(jsonString)
        
        assertNotNull(event)
        assertEquals("kitchen_orders_update", event.type)
        assertEquals(1, event.orders.size)
        
        val order = event.orders.first()
        assertEquals(29, order.id)
        assertEquals(1, order.tableNumber)
        assertEquals("test-user", order.userName)
        assertEquals(1, order.items.size)
        
        val item = order.items.first()
        assertEquals(1, item.itemId)
        assertEquals("Espetinho de Alcatra", item.name)
        assertEquals(1, item.totalCount)
        assertEquals(null, item.observation)
        assertEquals(ItemStatus.OPEN, item.overallStatus)
        assertEquals(ItemCategory.SKEWER, item.category)
        assertEquals(1, item.unitStatuses.size)
        
        val unitStatus = item.unitStatuses.first()
        assertEquals(0, unitStatus.unitIndex)
        assertEquals(ItemStatus.OPEN, unitStatus.status)
        assertEquals(1755707429754, unitStatus.updatedAt)
        assertEquals(null, unitStatus.updatedBy)
    }
}