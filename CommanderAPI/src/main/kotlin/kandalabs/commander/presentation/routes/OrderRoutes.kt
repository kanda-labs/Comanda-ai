package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kandalabs.commander.domain.model.OrderResponse
import kandalabs.commander.domain.service.OrderService
import kandalabs.commander.presentation.models.request.CreateOrderRequest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Route.orderRoutes(orderService: OrderService) {
    route("/orders") {
        get {
            call.respond(orderService.getAllOrders())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val order = orderService.getOrderById(id)
            if (order != null) {
                call.respond(order)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post {
            val orderRequest = call.receive<CreateOrderRequest>()

            call.respond(HttpStatusCode.Created, orderService.createOrder(orderRequest))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val orderResponse = call.receive<OrderResponse>()
            val updatedOrder = orderService.updateOrder(id, orderResponse)
            if (updatedOrder != null) {
                call.respond(updatedOrder)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (orderService.deleteOrder(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

internal fun localDateTimeNow(): LocalDateTime = Instant.fromEpochMilliseconds( System.currentTimeMillis())
        .toLocalDateTime(TimeZone.currentSystemDefault())


internal fun localDateTimeAsLong(): Long {
    return Instant.fromEpochMilliseconds(System.currentTimeMillis()).toEpochMilliseconds()
}


