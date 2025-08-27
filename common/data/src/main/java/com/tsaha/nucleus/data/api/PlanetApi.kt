package com.tsaha.nucleus.data.api

import com.tsaha.nucleus.data.model.PaginationInfo
import com.tsaha.nucleus.data.model.Planet

/**
 * API interface for planet-related operations
 */
interface PlanetApi {
    /**
     * Fetches planets from the SWAPI with pagination
     * @param pageNumber The page number to fetch (starting from 1)
     * @param limit The number of planets per page
     * @return Pair of PaginationInfo and List of Planets
     */
    suspend fun getPlanets(pageNumber: Int, limit: Int): Result<Pair<PaginationInfo, List<Planet>>>
}