package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.data.api.PlanetApi
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails

/**
 * Implementation of PlanetRepository using remote API
 * Acts as a single source of truth for planet data
 * @param planetApi The API interface for fetching planet data
 */
class PlanetRepositoryImpl(
    private val planetApi: PlanetApi
) : PlanetRepository {

    override val cache: MutableMap<String, PlanetDetails> = mutableMapOf()

    override suspend fun getPlanetsWithPagination(
        pageNumber: Int,
        limit: Int
    ): Result<Pair<Pagination, List<Planet>>> {
        return try {
            require(pageNumber >= 1) { "Page number must be >= 1" }
            require(limit > 0) { "Limit must be > 0" }
            planetApi.getPlanets(pageNumber, limit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlanetsWithPagination(limit: Int): Result<Pair<Pagination, List<Planet>>> {
        return getPlanetsWithPagination(pageNumber = 1, limit = limit)
    }

    override suspend fun getPlanet(id: String): Result<PlanetDetails> {
        return try {
            require(id.isNotBlank()) { "Planet ID cannot be blank" }
            planetApi.getPlanet(id).onSuccess { cache[id] = it }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}