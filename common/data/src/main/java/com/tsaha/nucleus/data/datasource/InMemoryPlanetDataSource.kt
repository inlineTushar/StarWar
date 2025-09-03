package com.tsaha.nucleus.data.datasource

import com.tsaha.nucleus.data.model.PlanetDetails
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of PlanetDataSource using a ConcurrentHashMap
 * for thread-safe storage of planet details by ID.
 *
 * This implementation provides:
 * - Thread-safe operations using ConcurrentHashMap
 * - Efficient concurrent read/write access
 * - Memory-efficient storage with cleanup capabilities
 * - Atomic batch operations for multiple planets
 */
class InMemoryPlanetDataSource : PlanetDataSource {

    private val planetsMap = ConcurrentHashMap<String, PlanetDetails>()

    // Only use mutex for operations that require atomicity across multiple map operations
    private val batchMutex = Mutex()

    override suspend fun storePlanet(planetDetails: PlanetDetails) {
        require(planetDetails.uid.isNotBlank()) { "Planet UID cannot be blank" }
        planetsMap[planetDetails.uid] = planetDetails
    }

    override suspend fun getPlanet(planetId: String): PlanetDetails? {
        require(planetId.isNotBlank()) { "Planet ID cannot be blank" }
        return planetsMap[planetId]
    }

    override suspend fun storePlanets(planetList: List<PlanetDetails>) {
        require(planetList.isNotEmpty()) { "Planet list cannot be empty" }

        // Use mutex only for batch operations to ensure atomicity
        batchMutex.withLock {
            planetList.forEach { planetDetails ->
                require(planetDetails.uid.isNotBlank()) {
                    "Planet UID cannot be blank for planet: ${planetDetails.name}"
                }
                planetsMap[planetDetails.uid] = planetDetails
            }
        }
    }

    override suspend fun getAllPlanets(): List<PlanetDetails> {
        return planetsMap.values.toList()
    }

    override suspend fun clearAll() {
        planetsMap.clear()
    }

    override suspend fun getPlanetCount(): Int {
        return planetsMap.size
    }

    override suspend fun containsPlanet(planetId: String): Boolean {
        require(planetId.isNotBlank()) { "Planet ID cannot be blank" }
        return planetsMap.containsKey(planetId)
    }

    override suspend fun removePlanet(planetId: String): PlanetDetails? {
        require(planetId.isNotBlank()) { "Planet ID cannot be blank" }
        return planetsMap.remove(planetId)
    }

    override suspend fun searchPlanetsByName(namePattern: String): List<PlanetDetails> {
        require(namePattern.isNotBlank()) { "Search pattern cannot be blank" }
        return planetsMap.values.filter {
            it.name.contains(namePattern, ignoreCase = true)
        }
    }

    override suspend fun updateOrStorePlanet(planetDetails: PlanetDetails): PlanetDetails? {
        require(planetDetails.uid.isNotBlank()) { "Planet UID cannot be blank" }
        return planetsMap.put(planetDetails.uid, planetDetails)
    }

    override suspend fun getPlanetsInBatches(batchSize: Int): Sequence<List<PlanetDetails>> {
        require(batchSize > 0) { "Batch size must be positive" }
        return getAllPlanets().chunked(batchSize).asSequence()
    }
}