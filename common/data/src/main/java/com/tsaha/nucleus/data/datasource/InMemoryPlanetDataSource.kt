package com.tsaha.nucleus.data.datasource

import com.tsaha.nucleus.data.model.PlanetDetails
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of PlanetDetailsDataSource using a ConcurrentHashMap
 * for thread-safe storage of planet details by ID
 */
class InMemoryPlanetDataSource : PlanetDataSource {

    private val planetsMap = ConcurrentHashMap<String, PlanetDetails>()
    private val mutex = Mutex()

    override suspend fun storePlanet(planetDetails: PlanetDetails) {
        mutex.withLock { planetsMap[planetDetails.uid] = planetDetails }
    }

    override suspend fun getPlanet(planetId: String): PlanetDetails? {
        return planetsMap[planetId]
    }

    override suspend fun storePlanets(planetList: List<PlanetDetails>) {
        mutex.withLock {
            planetList.forEach { planetDetails ->
                planetsMap[planetDetails.uid] = planetDetails
            }
        }
    }

    override suspend fun getAllPlanets(): List<PlanetDetails> {
        return planetsMap.values.toList()
    }

    override suspend fun clearAll() {
        mutex.withLock {
            planetsMap.clear()
        }
    }
}