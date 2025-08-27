package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.data.model.PaginationInfo
import com.tsaha.nucleus.data.model.Planet

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
    suspend fun getPlanets(
        pageNumber: Int = 1,
        limit: Int = 10
    ): Result<Pair<PaginationInfo, List<Planet>>>

    /**
     * Fetches the first page of planets with default limit
     * @param limit The number of planets per page (default: 10)
     * @return Result containing Pair of PaginationInfo and List of Planets
     */
    suspend fun getFirstPage(limit: Int = 10): Result<Pair<PaginationInfo, List<Planet>>>
}