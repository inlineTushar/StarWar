package com.tsaha.nucleus.data.datasource

import com.tsaha.nucleus.data.model.PlanetDetails

/**
 * Data source interface for storing and retrieving planet details by ID.
 *
 * This interface provides comprehensive CRUD operations and utility methods
 * for managing planet data in memory or persistent storage.
 */
interface PlanetDataSource {

    /**
     * Store planet details with the planet's UID as the key
     * @param planetDetails the planet details to store
     * @throws IllegalArgumentException if planet UID is blank
     */
    suspend fun storePlanet(planetDetails: PlanetDetails)

    /**
     * Retrieve planet details by ID
     * @param planetId the ID of the planet to retrieve
     * @return the planet details if found, null otherwise
     * @throws IllegalArgumentException if planetId is blank
     */
    suspend fun getPlanet(planetId: String): PlanetDetails?

    /**
     * Store multiple planet details at once (atomic operation)
     * @param planetList list of planet details to store
     * @throws IllegalArgumentException if list is empty or contains planets with blank UIDs
     */
    suspend fun storePlanets(planetList: List<PlanetDetails>)

    /**
     * Get all stored planet details
     * @return list of all stored planet details (may be empty)
     */
    suspend fun getAllPlanets(): List<PlanetDetails>

    /**
     * Clear all stored planet details
     */
    suspend fun clearAll()

    /**
     * Additional utility methods for enhanced functionality
     */

    /**
     * Get the total number of stored planets
     * @return count of stored planets
     */
    suspend fun getPlanetCount(): Int

    /**
     * Check if a planet exists by ID
     * @param planetId the ID to check
     * @return true if planet exists, false otherwise
     * @throws IllegalArgumentException if planetId is blank
     */
    suspend fun containsPlanet(planetId: String): Boolean

    /**
     * Remove a specific planet by ID
     * @param planetId the ID of the planet to remove
     * @return the removed planet details, or null if not found
     * @throws IllegalArgumentException if planetId is blank
     */
    suspend fun removePlanet(planetId: String): PlanetDetails?

    /**
     * Search for planets by name pattern (case-insensitive)
     * @param namePattern the pattern to match against planet names
     * @return list of matching planets (may be empty)
     * @throws IllegalArgumentException if namePattern is blank
     */
    suspend fun searchPlanetsByName(namePattern: String): List<PlanetDetails>

    /**
     * Update an existing planet or store if not exists
     * @param planetDetails the planet details to update/store
     * @return the previous planet details if it existed, null otherwise
     * @throws IllegalArgumentException if planet UID is blank
     */
    suspend fun updateOrStorePlanet(planetDetails: PlanetDetails): PlanetDetails?

    /**
     * Get planets in batches for memory-efficient processing
     * @param batchSize the maximum number of planets per batch
     * @return sequence of planet batches
     * @throws IllegalArgumentException if batchSize <= 0
     */
    suspend fun getPlanetsInBatches(batchSize: Int): Sequence<List<PlanetDetails>> {
        require(batchSize > 0) { "Batch size must be positive" }
        return getAllPlanets().chunked(batchSize).asSequence()
    }
}
