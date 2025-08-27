package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.data.api.PlanetApi
import com.tsaha.nucleus.data.model.PaginationInfo
import com.tsaha.nucleus.data.model.Planet

/**
 * Implementation of PlanetRepository using remote API
 * Acts as a single source of truth for planet data
 * @param planetApi The API interface for fetching planet data
 */
class PlanetRepositoryImpl(
    private val planetApi: PlanetApi
) : PlanetRepository {

    override suspend fun getPlanets(
        pageNumber: Int,
        limit: Int
    ): Result<Pair<PaginationInfo, List<Planet>>> {
        return try {
            // Validate parameters
            require(pageNumber >= 1) { "Page number must be >= 1" }
            require(limit > 0) { "Limit must be > 0" }

            planetApi.getPlanets(pageNumber, limit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFirstPage(limit: Int): Result<Pair<PaginationInfo, List<Planet>>> {
        return getPlanets(pageNumber = 1, limit = limit)
    }
}