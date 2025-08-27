package kandalabs.commander.presentation.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kandalabs.commander.domain.model.Bill
import kandalabs.commander.domain.model.BillStatus
import kandalabs.commander.domain.model.PartialPayment
import kandalabs.commander.domain.model.TableStatus
import kandalabs.commander.domain.service.BillService
import kandalabs.commander.domain.service.TableService
import kandalabs.commander.presentation.models.request.CreateBillRequest
import kandalabs.commander.presentation.models.request.CreatePartialPaymentRequest

fun Route.billRoutes(billService: BillService, tableService: TableService) {
    route("/bills") {
        get {
            val status = call.request.queryParameters["status"]?.let { BillStatus.valueOf(it) }
            call.respond(billService.getAllBills(status))
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val bill = billService.getBillById(id.toInt())
            if (bill != null) {
                call.respond(bill)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/table/{tableId}") {
            val tableId = call.parameters["tableId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val bill = billService.getBillByTableId(tableId.toInt())
            if (bill != null) {
                call.respond(bill)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/table/{tableId}/payment-summary") {
            val tableId = call.parameters["tableId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val paymentSummary = billService.getBillPaymentSummary(tableId.toInt())
            if (paymentSummary != null) {
                call.respond(paymentSummary)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post {
            runCatching {
                val request = call.receive<CreateBillRequest>()
                val bill = Bill(
                    id = null,
                    tableId = request.tableId,
                    tableNumber = request.tableNumber,
                    status = BillStatus.OPEN,
                    createdAt = localDateTimeNow(),
                    orders = emptyList()
                )

                val createdBill = billService.createBill(bill)
                request.tableId?.let { tableId ->
                    tableService.updateTable(
                        tableId = tableId,
                        newBillId = createdBill.id,
                        newStatus = TableStatus.OPEN
                    )
                }

                call.respond(HttpStatusCode.Created)
            }.fold(
                onSuccess = { call.respond(HttpStatusCode.Created) },
                onFailure = { exception ->
                    call.respond(HttpStatusCode.InternalServerError, "An unexpected error occurred $exception")
                })
        }

        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val bill = call.receive<Bill>()
            val updatedBill = billService.updateBill(id.toInt(), bill)
            if (updatedBill != null) {
                call.respond(updatedBill)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post("/table/{tableId}/payment") {
            val tableId = call.parameters["tableId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val success = billService.processTablePayment(tableId.toInt())
            if (success) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Payment processed successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Table or bill not found"))
            }
        }

        post("/table/{tableId}/partial-payment") {
            val tableId = call.parameters["tableId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            
            runCatching {
                val request = call.receive<CreatePartialPaymentRequest>()
                
                // Get bill for this table
                val bill = billService.getBillByTableId(tableId.toInt())
                    ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "Bill not found for this table"))
                
                val partialPayment = PartialPayment(
                    billId = bill.id!!,
                    tableId = tableId.toInt(),
                    paidBy = request.paidBy,
                    amountInCentavos = request.amountInCentavos,
                    amountFormatted = "R$ ${request.amountInCentavos / 100},${(request.amountInCentavos % 100).toString().padStart(2, '0')}",
                    description = request.description,
                    paymentMethod = request.paymentMethod,
                    createdAt = localDateTimeNow()
                )
                
                val createdPayment = billService.createPartialPayment(partialPayment)
                call.respond(HttpStatusCode.Created, createdPayment)
            }.fold(
                onSuccess = { },
                onFailure = { exception ->
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to create partial payment: ${exception.message}"))
                }
            )
        }

        get("/table/{tableId}/partial-payments") {
            val tableId = call.parameters["tableId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val partialPayments = billService.getPartialPayments(tableId.toInt())
            call.respond(partialPayments)
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (billService.deleteBill(id.toInt())) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

