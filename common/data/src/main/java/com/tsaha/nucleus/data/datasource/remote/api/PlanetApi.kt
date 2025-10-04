package com.tsaha.nucleus.data.datasource.remote.api

import com.tsaha.nucleus.data.model.PaginationApiModel
import com.tsaha.nucleus.data.model.PlanetApiModel
import com.tsaha.nucleus.data.model.PlanetDetailsApiModel

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
    suspend fun getPlanets(pageNumber: Int, limit: Int): Result<Pair<PaginationApiModel, List<PlanetApiModel>>>

    /**
     * Fetches detailed information for a specific planet
     * @param id The planet ID (uid) to fetch details for
     * @return PlanetDetail with detailed information
     */
    suspend fun getPlanet(id: String): Result<PlanetDetailsApiModel>
}