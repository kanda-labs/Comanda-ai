package co.touchlab.dogify.data.models

import org.junit.Test
import kotlin.test.assertEquals

class BreedsAndSubBreedsResponseTest {

    @Test
    fun `BreedsAndSubBreedsResponse toDomain should return a list of Breed objects`() {
        val response = BreedsAndSubBreedsResponse(
            status = "success",
            breeds = mapOf(
                "breed1" to listOf("subbreed1", "subbreed2"),
                "breed2" to emptyList()
            )
        )
        val domain = response.toDomain()
        assertEquals(3, domain.size)
    }
}