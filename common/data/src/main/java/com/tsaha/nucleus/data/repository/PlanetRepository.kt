package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.core.network.PAGE_SIZE
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails

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
    ): Result<Pair<Pagination, List<Planet>>>

    /**
     * Fetches the first page of planets with default limit
     * @param limit The number of planets per page (default: 10)
     * @return Result containing Pair of PaginationInfo and List of Planets
     */
    suspend fun getPlanetsWithPagination(limit: Int = 10): Result<Pair<Pagination, List<Planet>>>

    /**
     * Fetches detailed information for a specific planet
     * @param id The planet ID (uid) to fetch details for
     * @return Result containing PlanetDetail with detailed information
     */
    suspend fun getPlanet(id: String): Result<PlanetDetails>
}