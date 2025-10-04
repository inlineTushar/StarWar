package com.tsaha.nucleus.data

import android.R.attr.gravity
import com.tsaha.nucleus.data.datasource.local.runtimememory.model.PlanetDetailsMemoryModel
import com.tsaha.nucleus.data.datasource.remote.model.PaginationApiModel
import com.tsaha.nucleus.data.datasource.remote.model.PlanetApiModel
import com.tsaha.nucleus.data.datasource.remote.model.PlanetDetailsApiModel
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails

fun Result<Pair<PaginationApiModel, List<PlanetApiModel>>>.asPaginationPlanetPair() =
    map { pair: Pair<PaginationApiModel, List<PlanetApiModel>> ->
        pair.first.asPagination() to pair.second.asPlanets()
    }

fun Result<PlanetDetailsApiModel>.asPlanetDetails() = map {
    PlanetDetails(
        uid = it.uid,
        name = it.name,
        climate = it.climate,
        population = it.population,
        diameter = it.diameter,
        gravity = it.gravity,
        terrain = it.terrain
    )
}

fun PaginationApiModel.asPagination() = Pagination(
    currentPage = currentPage,
    nextPage = nextPage
)

fun List<PlanetApiModel>.asPlanets() = map { it.asPlanet() }

private  fun PlanetApiModel.asPlanet() = Planet(uid = uid, name = name)

fun PlanetDetailsApiModel.asMemoryPlanetDetails() =
    PlanetDetailsMemoryModel(
        uid = uid,
        name = name,
        climate = climate,
        population = population,
        diameter = diameter,
        gravity = gravity,
        terrain = terrain
    )

fun PlanetDetailsMemoryModel.asPlanetDetails() =
    PlanetDetails(
        uid = uid,
        name = name,
        climate = climate,
        population = population,
        diameter = diameter,
        gravity = gravity,
        terrain = terrain
    )