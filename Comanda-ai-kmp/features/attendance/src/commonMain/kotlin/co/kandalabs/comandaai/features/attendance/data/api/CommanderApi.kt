package co.kandalabs.comandaai.features.attendance.data.api

import co.kandalabs.comandaai.features.attendance.data.repository.CreateOrderRequest
import co.kandalabs.comandaai.features.attendance.data.repository.CreateBillRequest
import co.kandalabs.comandaai.features.attendance.data.repository.UpdateTableRequest
import co.kandalabs.comandaai.features.attendance.data.repository.UpdateOrderWithStatusesRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.Path
import co.kandalabs.comandaai.features.attendance.domain.models.model.Bill
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.features.attendance.domain.models.model.Order
import co.kandalabs.comandaai.features.attendance.domain.models.model.OrderWithStatuses
import co.kandalabs.comandaai.features.attendance.domain.models.model.PartialPayment
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreatePartialPaymentRequest
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.models.model.PaymentSummaryResponse
import co.kandalabs.comandaai.features.attendance.data.models.TableMigrationResponse
import co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails.PartialPaymentDetails

internal interface CommanderApi {
    companion object {
        // Base URL is now configured per environment
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
    
    @POST("api/v1/bills/table/{tableId}/partial-payment")
    suspend fun createPartialPayment(@Path("tableId") tableId: Int, @Body request: CreatePartialPaymentRequest): PartialPayment
    
    @GET("api/v1/bills/table/{tableId}/partial-payments")
    suspend fun getPartialPayments(@Path("tableId") tableId: Int): List<PartialPayment>
    
    @POST("api/v1/tables/{originId}/migrate/{destinationId}")
    suspend fun migrateTable(@Path("originId") originId: Int, @Path("destinationId") destinationId: Int): TableMigrationResponse

    @GET("api/v1/bills/partial-payments/{paymentId}")
    suspend fun getPartialPaymentDetails(@Path("paymentId") paymentId: Int): PartialPaymentDetails

    @PATCH("api/v1/bills/partial-payments/{paymentId}/cancel")
    suspend fun cancelPartialPayment(@Path("paymentId") paymentId: Int)
}
