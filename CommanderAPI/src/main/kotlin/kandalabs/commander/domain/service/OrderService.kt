package kandalabs.commander.domain.service

import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.OrderResponse
import kandalabs.commander.domain.model.OrderWithStatusesResponse
import kandalabs.commander.domain.repository.OrderRepository
import kandalabs.commander.presentation.models.request.CreateOrderRequest

class OrderService(private val orderRepository: OrderRepository) {

    suspend fun getAllOrders(): List<OrderResponse> {
        return orderRepository.getAllOrders()
    }

    suspend fun getOrderById(id: Int): OrderResponse? {
        return orderRepository.getOrderById(id)
    }

    suspend fun getOrderByIdWithStatuses(id: Int): OrderWithStatusesResponse? {
        return orderRepository.getOrderByIdWithStatuses(id)
    }

    suspend fun createOrder(orderRequest: CreateOrderRequest): OrderResponse {
        return orderRepository.createOrder(orderRequest)
    }

    suspend fun updateOrder(id: Int, orderResponse: OrderResponse): OrderResponse? {
        return orderRepository.updateOrder(id, orderResponse)
    }

    suspend fun updateOrderWithIndividualStatuses(
        id: Int,
        orderResponse: OrderResponse,
        individualStatuses: Map<String, ItemStatus>,
        updatedBy: String
    ): OrderResponse? {
        return orderRepository.updateOrderWithIndividualStatuses(id, orderResponse, individualStatuses, updatedBy)
    }

    suspend fun deleteOrder(id: Int): Boolean {
        return orderRepository.deleteOrder(id)
    }

    suspend fun getOrdersByBillId(billId: Int): List<OrderResponse> {
        return orderRepository.getOrdersByBillId(billId)
    }
}
