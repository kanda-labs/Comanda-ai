package co.kandalabs.comandaai.data.api

import co.kandalabs.comandaai.data.repository.CreateOrderRequest
import co.kandalabs.comandaai.data.repository.CreateBillRequest
import co.kandalabs.comandaai.data.repository.UpdateTableRequest
import co.kandalabs.comandaai.data.repository.UpdateOrderWithStatusesRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderWithStatuses
import kandalabs.commander.domain.model.Table
import co.kandalabs.comandaai.domain.models.model.PaymentSummaryResponse

internal interface CommanderApi {
    companion object {
        const val baseUrl = "http://192.168.0.161:8081/"
    }

    @GET("api/v1/items")
    suspend fun getItems(): List<Item>

    @GET("api/v1/tables")
    suspend fun getTables(): List<Table>

    @GET("api/v1/tables/{id}")
    suspend fun getTable(@Path("id") id: Int): Table

    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Order

    @POST("api/v1/bills")
    suspend fun createBill(@Body request: CreateBillRequest)

    @PUT("api/v1/tables/{id}")
    suspend fun updateTable(@Path("id") id: Int, @Body request: UpdateTableRequest): Table
    
    @GET("api/v1/orders")
    suspend fun getAllOrders(): List<Order>
    
    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Order

    @GET("api/v1/orders/{id}/with-statuses")
    suspend fun getOrderByIdWithStatuses(@Path("id") id: Int): OrderWithStatuses
    
    @PUT("api/v1/orders/{id}")
    suspend fun updateOrder(@Path("id") id: Int, @Body order: Order): Order
    
    @PUT("api/v1/orders/{id}/with-statuses")
    suspend fun updateOrderWithIndividualStatuses(@Path("id") id: Int, @Body request: UpdateOrderWithStatusesRequest): Order
    
    @GET("api/v1/bills/table/{tableId}")
    suspend fun getBillByTableId(@Path("tableId") tableId: Int): Bill

    @GET("api/v1/bills/table/{tableId}/payment-summary")
    suspend fun getPaymentSummary(@Path("tableId") tableId: Int): PaymentSummaryResponse
    
    @PUT("api/v1/bills/{id}")
    suspend fun updateBill(@Path("id") id: Int, @Body bill: Bill): Bill

    @POST("api/v1/bills/table/{tableId}/payment")
    suspend fun processTablePayment(@Path("tableId") tableId: Int)
}
