package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.core.network.PAGE_SIZE
import com.tsaha.nucleus.data.model.PaginationApiModel
import com.tsaha.nucleus.data.model.PlanetApiModel
import com.tsaha.nucleus.data.model.PlanetDetailsApiModel

/**
 * Repository interface for planet-related data operations
 * Acts as a contract for accessing planet data from various sources
 */
interface PlanetRepository {

    /**
     * Fetches planets from the remote API
     * @param pageNumber The page number to fetch (starting from 1)
     * @param limit The number of planets per page (default: 10)
     * @return Result containing Pair of PaginationInfo and List of Planets
     */
    suspend fun getPlanetsWithPagination(
        pageNumber: Int = 1,
        limit: Int = PAGE_SIZE
    ): Result<Pair<PaginationApiModel, List<PlanetApiModel>>>

    /**
     * Fetches the first page of planets with default limit
     * @param limit The number of planets per page (default: 10)
     * @return Result containing Pair of PaginationInfo and List of Planets
     */
    suspend fun getPlanetsWithPagination(limit: Int = 10): Result<Pair<PaginationApiModel, List<PlanetApiModel>>>

    /**
     * Fetches detailed information for a specific planet
     * @param id The planet ID (uid) to fetch details for
     * @return Result containing PlanetDetail with detailed information
     */
    suspend fun getPlanet(id: String): Result<PlanetDetailsApiModel>

    /**
     * Searches for planets by name using the local data source
     * @param query The search query to filter planet names
     * @return Result containing list of matching planets with details
     */
    suspend fun searchPlanets(query: String): Result<List<PlanetDetailsApiModel>>
}