package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.data.asMemoryPlanetDetails
import com.tsaha.nucleus.data.asPaginationPlanetPair
import com.tsaha.nucleus.data.asPlanetDetails
import com.tsaha.nucleus.data.datasource.local.runtimememory.PlanetLocalDataSource
import com.tsaha.nucleus.data.datasource.remote.PlanetRemoteDataSource
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PlanetRepository using remote API.
 * Acts as a single source of truth for planet data.
 */
@Singleton
class PlanetRepositoryImpl @Inject constructor(
    private val remoteSource: PlanetRemoteDataSource,
    private val localSource: PlanetLocalDataSource
) : PlanetRepository {

    override suspend fun getPlanetsWithPagination(
        pageNumber: Int,
        limit: Int
    ): Result<Pair<Pagination, List<Planet>>> {
        return try {
            require(pageNumber >= 1) { "Page number must be >= 1" }
            require(limit > 0) { "Limit must be > 0" }
            remoteSource.getPlanets(pageNumber, limit).asPaginationPlanetPair()
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlanetsWithPagination(limit: Int):
            Result<Pair<Pagination, List<Planet>>> {
        return getPlanetsWithPagination(pageNumber = 1, limit = limit)
    }

    override suspend fun getPlanet(id: String): Result<PlanetDetails> {
        return try {
            require(id.isNotBlank()) { "Planet ID cannot be blank" }
            localSource.getPlanet(planetId = id)?.let { storedPlanet ->
                Result.success(storedPlanet.asPlanetDetails())
            } ?: run {
                val remotePlanet = remoteSource.getPlanet(id)
                remotePlanet.onSuccess { planetDetails ->
                    localSource.storePlanet(planetDetails.asMemoryPlanetDetails())
                }
                remotePlanet.asPlanetDetails()
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPlanets(query: String): Result<List<PlanetDetails>> {
        return try {
            require(query.isNotBlank()) { "Search query cannot be blank" }
            val allPlanets = localSource.getAllPlanets()
            val filteredPlanets = allPlanets.filter { planet ->
                planet.name.contains(query, ignoreCase = true)
            }
            val filteredPlanetsDetails = filteredPlanets.map { it.asPlanetDetails() }
            Result.success(filteredPlanetsDetails)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}