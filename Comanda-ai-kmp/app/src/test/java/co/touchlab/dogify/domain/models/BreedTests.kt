package co.touchlab.dogify.domain.models

import junit.framework.TestCase.assertEquals
import kotlin.test.Test

class BreedTests {

    @Test
    fun `breed display name should presented properly`(){
        val breed = Breed(name = "bulldog", subBreedName = "boston")
        assertEquals("Boston Bulldog", breed.displayName)

        val breedWithoutSubBreed = Breed(name = "bulldog")
        assertEquals("Bulldog", breedWithoutSubBreed.displayName)
    }
}