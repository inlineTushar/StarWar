package com.tsaha.nucleus.data.datasource

import com.tsaha.nucleus.data.model.PlanetDetails

/**
 * Data source interface for storing and retrieving planet details by ID
 */
interface PlanetDataSource {

    /**
     * Store planet details with the planet's UID as the key
     * @param planetDetails the planet details to store
     */
    suspend fun storePlanet(planetDetails: PlanetDetails)

    /**
     * Retrieve planet details by ID
     * @param planetId the ID of the planet to retrieve
     * @return the planet details if found, null otherwise
     */
    suspend fun getPlanet(planetId: String): PlanetDetails?

    /**
     * Store multiple planet details at once
     * @param planetList list of planet details to store
     */
    suspend fun storePlanets(planetList: List<PlanetDetails>)

    /**
     * Get all stored planet details
     * @return list of all stored planet details
     */
    suspend fun getAllPlanets(): List<PlanetDetails>

    /**
     * Clear all stored planet details
     */
    suspend fun clearAll()
}
