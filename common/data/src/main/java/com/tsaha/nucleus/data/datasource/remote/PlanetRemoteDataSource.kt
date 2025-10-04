package com.tsaha.nucleus.data.datasource.remote

import com.tsaha.nucleus.data.datasource.remote.api.PlanetApi
import com.tsaha.nucleus.data.datasource.remote.model.PaginationApiModel
import com.tsaha.nucleus.data.datasource.remote.model.PlanetApiModel
import com.tsaha.nucleus.data.datasource.remote.model.PlanetDetailsApiModel

class PlanetRemoteDataSource(private val planetApi: PlanetApi) {

    suspend fun getPlanets(pageNumber: Int, limit: Int):
            Result<Pair<PaginationApiModel, List<PlanetApiModel>>> =
        planetApi.getPlanets(pageNumber, limit)

    suspend fun getPlanet(id: String): Result<PlanetDetailsApiModel> = planetApi.getPlanet(id)
}
