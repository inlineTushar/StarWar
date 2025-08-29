package com.tsaha.planetlist

import com.tsaha.nucleus.core.network.CONCURRENT_REQUESTS
import com.tsaha.nucleus.core.network.PAGE_SIZE
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsError
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsSuccess
import com.tsaha.planetlist.model.PlanetItem
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListError
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import com.tsaha.planetlist.model.PlanetListUiState.ListSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlanetListUseCase(
    private val planetRepository: PlanetRepository
) {
    fun observePlanets(
        concurrency: Int = CONCURRENT_REQUESTS,
        pageSize: Int = PAGE_SIZE
    ): Flow<PlanetListUiState> = channelFlow {
        send(ListLoading)

        val planetsResult = planetRepository.getPlanetsWithPagination(pageSize)
        val planets: List<Planet> = planetsResult.getOrNull()?.second.orEmpty()

        if (planets.isEmpty()) {
            val msg = planetsResult.exceptionOrNull()?.message
            send(ListError(msg))
            return@channelFlow
        }

        val currentItems = planets.map { planet ->
            PlanetItem(
                planet = planet,
                detailsState = DetailsLoading
            )
        }.toMutableList()

        send(ListSuccess(currentItems.toList()))

        val indexById = planets.mapIndexed { idx, p -> p.uid to idx }.toMap()

        planets.asFlow()
            .flatMapMerge(concurrency = concurrency) { planet ->
                flow {
                    val planetDetails =
                        planetRepository.getPlanet(planet.uid).getOrNull()
                            ?.let { DetailsSuccess(it) }
                            ?: DetailsError()
                    emit(planet to planetDetails)
                }
            }
            .collect { (planet, planetDetails) ->
                indexById[planet.uid]?.let { idx ->
                    if (currentItems[idx].detailsState is DetailsLoading) {
                        currentItems[idx] = currentItems[idx].copy(detailsState = planetDetails)
                        send(ListSuccess(currentItems.toList()))
                    }
                }
            }
    }.flowOn(Dispatchers.IO)
}