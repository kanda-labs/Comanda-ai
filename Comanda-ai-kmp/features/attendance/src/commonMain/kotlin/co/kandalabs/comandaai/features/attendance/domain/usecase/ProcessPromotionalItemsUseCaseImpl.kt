package co.kandalabs.comandaai.features.attendance.domain.usecase

import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.OrderRepository
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateOrderItemRequest

class ProcessPromotionalItemsUseCaseImpl(
    private val itemsRepository: ItemsRepository,
    private val orderRepository: OrderRepository
) : ProcessPromotionalItemsUseCase {

    private val promotionalCombos = mapOf(
        19 to listOf(
            PromotionalItem(itemId = 28, name = "Promo Batata Frita", count = 1),
            PromotionalItem(itemId = 29, name = "Promo Chopp", count = 2)
        ),
        20 to listOf(
            PromotionalItem(itemId = 30, name = "Promo Bolinho Queijo", count = 1),
            PromotionalItem(itemId = 29, name = "Promo Chopp", count = 2)
        ),
        27 to listOf(
            PromotionalItem(itemId = 31, name = "Promo Coxinha", count = 1),
            PromotionalItem(itemId = 29, name = "Promo Chopp", count = 2)
        )
    )

    override suspend fun processPromotionalItems(
        selectedItems: List<CreateOrderItemRequest>
    ): ProcessPromotionalItemsResult {
        val resultItems = selectedItems.toMutableList()
        val promotionalItemIds = mutableListOf<Int>()
        
        // Buscar todos os itens para verificar categorias
        val allItemsResult = itemsRepository.getItems(itemStatus = null)
        if (allItemsResult !is ComandaAiResult.Success) {
            return ProcessPromotionalItemsResult(selectedItems, emptyList())
        }
        
        val allItems = allItemsResult.data
        
        // Identificar itens promocionais selecionados
        selectedItems.forEach { selectedItem ->
            val item = allItems.find { it.id == selectedItem.itemId }
            if (item?.category == ItemCategory.PROMOTIONAL) {
                promotionalItemIds.add(selectedItem.itemId)
                
                // Verificar se é um combo promocional e adicionar itens gratuitos
                promotionalCombos[selectedItem.itemId]?.let { promoItems ->
                    promoItems.forEach { promoItem ->
                        // Adicionar item promocional com valor zerado
                        resultItems.add(
                            CreateOrderItemRequest(
                                itemId = promoItem.itemId,
                                name = promoItem.name,
                                count = promoItem.count * selectedItem.count,
                                observation = "Item promocional"
                            )
                        )
                    }
                }
            }
        }
        
        return ProcessPromotionalItemsResult(resultItems, promotionalItemIds)
    }
    
    override suspend fun markPromotionalItemsAsDelivered(
        order: Order,
        promotionalItemIds: List<Int>,
        updatedBy: String
    ): ComandaAiResult<Order> {
        if (promotionalItemIds.isEmpty()) {
            return ComandaAiResult.Success(order)
        }
        
        // Criar mapa de status individuais, marcando apenas os itens promocionais como DELIVERED
        val individualStatuses = mutableMapOf<String, ItemStatus>()
        
        order.items?.forEach { item ->
            if (promotionalItemIds.contains(item.itemId)) {
                // Marcar cada unidade do item do combo original como DELIVERED
                // (apenas os IDs 19, 20, 27 - os itens gerados ficam PENDING para a cozinha)
                (0 until item.count).forEach { unitIndex ->
                    individualStatuses["${item.itemId}_${unitIndex}"] = ItemStatus.DELIVERED
                }
            }
        }
        
        return if (individualStatuses.isNotEmpty()) {
            orderRepository.updateOrderWithIndividualStatuses(
                orderId = order.id!!,
                order = order,
                individualStatuses = individualStatuses,
                updatedBy = updatedBy
            )
        } else {
            ComandaAiResult.Success(order)
        }
    }
    
    private data class PromotionalItem(
        val itemId: Int,
        val name: String,
        val count: Int
    )
}