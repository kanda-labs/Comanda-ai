package co.touchlab.dogify.fakes

import co.touchlab.dogify.data.repository.LocalBreedRepository
import co.touchlab.dogify.domain.models.PageConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.min

internal class FakeLocalBreedRepository : LocalBreedRepository {

    var countSpy = 0
    var localBreeds: ImmutableList<Breed> = persistentListOf()

    override suspend fun isEmpty(): Boolean =
        localBreeds.isEmpty()

    override suspend fun storeBreeds(breeds: List<Breed>) {
        localBreeds = breeds.toPersistentList()
    }

    override suspend fun updateBreeds(breeds: List<Breed>) {
        val mapOfLocalBreeds = localBreeds.associateBy { it.id }
        val updatedBreeds = breeds.map { breed ->
            mapOfLocalBreeds[breed.id]?.let { localBreed ->
                breed.copy(url = localBreed.url)
            } ?: breed
        }
        localBreeds = updatedBreeds.toPersistentList()
    }

    override suspend fun getPageItems(pageConfig: PageConfig): ImmutableList<Breed> {
        return localBreeds.subList(
            fromIndex = min(pageConfig.offset, localBreeds.size),
            toIndex = (pageConfig.offset + pageConfig.size).coerceAtMost(localBreeds.size)
        ).toPersistentList()
    }

    override suspend fun count(): Long {
        countSpy
        return localBreeds.size.toLong()
    }

}