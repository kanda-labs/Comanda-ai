package co.touchlab.dogify.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BreedImageResponse(
    val status: String,
    @SerialName("message") val url: String,
)