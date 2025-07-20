package kandalabs.commander.presentation.models.request

import kandalabs.commander.domain.model.TableStatus
import kotlinx.serialization.Serializable

@Serializable
data class CreateTableRequest(
    val number: Int,
)

@Serializable
data class UpdateTableRequest(
    val billId: Int? = null,
    val status: TableStatus? = null
)
