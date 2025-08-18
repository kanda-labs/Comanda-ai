package kandalabs.commander.presentation.models.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateItemStatusRequest(
    val status: String
)