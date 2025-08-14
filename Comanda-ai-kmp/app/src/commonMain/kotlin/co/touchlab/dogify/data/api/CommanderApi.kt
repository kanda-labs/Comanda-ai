package co.touchlab.dogify.data.api

import co.touchlab.dogify.data.repository.CreateOrderRequest
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table

internal interface CommanderApi {
    companion object {
        const val baseUrl = "http://10.0.2.2:8081/"
    }

    @GET("api/v1/items")
    suspend fun getItems(): List<Item>

    @GET("api/v1/tables")
    suspend fun getTables(): List<Table>

    @GET("api/v1/tables/{id}")
    suspend fun getTable(@Path("id") id: Int): Table

    @POST("api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Order
}
