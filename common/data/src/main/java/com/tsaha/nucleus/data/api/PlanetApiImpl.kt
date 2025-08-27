package com.tsaha.nucleus.data.api

import com.tsaha.nucleus.data.model.PaginationInfo
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetail
import com.tsaha.nucleus.data.model.PlanetsApiResponse
import com.tsaha.nucleus.data.model.PlanetDetailApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Ktor implementation of PlanetApi
 * @param httpClient The configured HTTP client for making API calls
 */
class PlanetApiImpl(
    private val httpClient: HttpClient
) : PlanetApi {

    companion object {
        private const val BASE_URL = "https://swapi.tech/api"
        private const val PLANETS_ENDPOINT = "$BASE_URL/planets"
    }

    override suspend fun getPlanets(
        pageNumber: Int,
        limit: Int
    ): Result<Pair<PaginationInfo, List<Planet>>> {
        return try {
            val response = httpClient.get(PLANETS_ENDPOINT) {
                parameter("page", pageNumber)
                parameter("limit", limit)
            }.body<PlanetsApiResponse>()

            val paginationInfo = PaginationInfo(
                currentPage = pageNumber,
                nextPage = response.next
            )
            val planets = response.results
            Result.success(paginationInfo to planets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlanet(id: String): Result<PlanetDetail> {
        return try {
            val response = httpClient.get("$PLANETS_ENDPOINT/$id")
                .body<PlanetDetailApiResponse>()

            val planetDetail = PlanetDetail(
                uid = response.result.uid,
                name = response.result.properties.name,
                climate = response.result.properties.climate,
                population = response.result.properties.population,
                diameter = response.result.properties.diameter,
                gravity = response.result.properties.gravity,
                terrain = response.result.properties.terrain
            )

            Result.success(planetDetail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}