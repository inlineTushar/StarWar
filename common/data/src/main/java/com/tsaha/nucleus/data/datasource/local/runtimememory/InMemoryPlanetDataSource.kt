package com.tsaha.nucleus.data.datasource.local.runtimememory

import com.tsaha.nucleus.data.model.PlanetDetailsMemoryModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of PlanetDetailsDataSource using a ConcurrentHashMap
 * for thread-safe storage of planet details by ID
 */
class InMemoryPlanetDataSource : PlanetLocalDataSource {
    private val planetsMap = ConcurrentHashMap<String, PlanetDetailsMemoryModel>()
    private val mutex = Mutex()

    override suspend fun storePlanet(planetDetails: PlanetDetailsMemoryModel) {
        mutex.withLock { planetsMap[planetDetails.uid] = planetDetails }
    }

    override suspend fun getPlanet(planetId: String): PlanetDetailsMemoryModel? {
        return planetsMap[planetId]
    }

    override suspend fun storePlanets(planetList: List<PlanetDetailsMemoryModel>) {
        mutex.withLock {
            planetList.forEach { planetDetails ->
                planetsMap[planetDetails.uid] = planetDetails
            }
        }
    }

    override suspend fun getAllPlanets(): List<PlanetDetailsMemoryModel> {
        return planetsMap.values.toList()
    }

    override suspend fun clearAll() {
        mutex.withLock {
            planetsMap.clear()
        }
    }
}