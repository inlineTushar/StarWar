package com.tsaha.nucleus.data.repository

import com.tsaha.nucleus.data.api.PlanetApi
import com.tsaha.nucleus.data.datasource.PlanetDataSource
import com.tsaha.nucleus.data.model.PaginationApiModel
import com.tsaha.nucleus.data.model.PlanetApiModel
import com.tsaha.nucleus.data.model.PlanetDetailsApiModel
import com.tsaha.nucleus.data.model.asApiModel
import com.tsaha.nucleus.data.model.asMemoryModel

/**
 * Implementation of PlanetRepository using remote API
 * Acts as a single source of truth for planet data
 * @param planetApi The API interface for fetching planet data
 */
class PlanetRepositoryImpl(
    private val planetApi: PlanetApi,
    private val planetDataSource: PlanetDataSource
) : PlanetRepository {

    override suspend fun getPlanetsWithPagination(
        pageNumber: Int,
        limit: Int
    ): Result<Pair<PaginationApiModel, List<PlanetApiModel>>> {
        return try {
            require(pageNumber >= 1) { "Page number must be >= 1" }
            require(limit > 0) { "Limit must be > 0" }
            planetApi.getPlanets(pageNumber, limit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlanetsWithPagination(limit: Int):
            Result<Pair<PaginationApiModel, List<PlanetApiModel>>> {
        return getPlanetsWithPagination(pageNumber = 1, limit = limit)
    }

    override suspend fun getPlanet(id: String): Result<PlanetDetailsApiModel> {
        return try {
            require(id.isNotBlank()) { "Planet ID cannot be blank" }
            planetDataSource.getPlanet(planetId = id)?.let { storedPlanet ->
                Result.success(storedPlanet.asApiModel())
            } ?: run {
                val remotePlanet = planetApi.getPlanet(id)
                remotePlanet.onSuccess { planetDetails ->
                    planetDataSource.storePlanet(planetDetails.asMemoryModel())
                }
                remotePlanet
            }
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPlanets(query: String): Result<List<PlanetDetailsApiModel>> {
        return try {
            require(query.isNotBlank()) { "Search query cannot be blank" }
            val allPlanets = planetDataSource.getAllPlanets()
            val filteredPlanets = allPlanets.filter { planet ->
                planet.name.contains(query, ignoreCase = true)
            }
            val filteredPlanetsApiModels = filteredPlanets.map { it.asApiModel() }
            Result.success(filteredPlanetsApiModels)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}