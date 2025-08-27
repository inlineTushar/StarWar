package com.tsaha.nucleus.data.api

import com.tsaha.nucleus.data.model.PaginationInfo
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetsApiResponse
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
}