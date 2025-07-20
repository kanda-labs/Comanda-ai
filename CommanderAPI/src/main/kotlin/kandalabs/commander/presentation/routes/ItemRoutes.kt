package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.service.ItemService
import kandalabs.commander.presentation.models.request.CreateItemRequest

fun Route.itemRoutes(itemService: ItemService) {
    route("/items") {
        get {
            call.respond(itemService.getAllItems())
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val item = itemService.getItemById(id.toInt())
            if (item != null) {
                call.respond(item)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post {
            val request = call.receive<CreateItemRequest>()
            val item = Item(
                id = null,
                name = request.name,
                value = request.value,
                category = request.category,
                description = request.description,
            )
            call.respond(HttpStatusCode.Created, itemService.createItem(item))
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val item = call.receive<Item>()
            val updatedItem = itemService.updateItem(id.toInt(), item)
            if (updatedItem != null) {
                call.respond(updatedItem)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (itemService.deleteItem(id.toInt())) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

