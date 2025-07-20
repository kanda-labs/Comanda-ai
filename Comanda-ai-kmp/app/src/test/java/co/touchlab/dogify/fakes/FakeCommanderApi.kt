import co.touchlab.dogify.data.api.CommanderApi
import co.touchlab.dogify.data.models.BreedImageResponse
import co.touchlab.dogify.data.models.BreedsAndSubBreedsResponse

internal class FakeCommanderApi : CommanderApi {
    var response: BreedsAndSubBreedsResponse? = null
    var shouldThrow: Throwable? = null
    var shouldThrowOnImage: Throwable? = null
    var callBreedsAndSubBreedsCount = 0
    var callGetBreedImageCount = 0

    override suspend fun getItems(): BreedsAndSubBreedsResponse {
        callBreedsAndSubBreedsCount++
        shouldThrow?.let { throw it }
        return response
            ?: BreedsAndSubBreedsResponse(status = "success", breeds = emptyMap()) // fallback
    }

    override suspend fun getBreedImage(name: String): BreedImageResponse {
        callGetBreedImageCount++
        shouldThrowOnImage?.let { throw it }
        return BreedImageResponse(
            status = "success",
            url = "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg"
        )
    }

    override suspend fun getSubBreedImage(
        name: String,
        subbreed: String
    ): BreedImageResponse {
        callGetBreedImageCount++
        return BreedImageResponse(
            status = "success",
            url = "https://images.dog.ceo/breeds/hound-afghan/n02088094_1003.jpg"
        )
    }
}