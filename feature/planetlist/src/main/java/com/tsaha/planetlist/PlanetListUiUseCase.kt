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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlanetListUiUseCase(
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

        val indexById = planets.mapIndexed { i, p -> p.uid to i }.toMap()

        val currentItems = planets.map { planet ->
            PlanetItem(
                planet = planet,
                detailsState = planetRepository.cache[planet.uid]?.let { DetailsSuccess(it) }
                    ?: DetailsLoading
            )
        }.toMutableList()

        send(ListSuccess(currentItems.toList()))

        val planetIds = planets.map { it.uid }

        planetIds.asFlow()
            .filter { it !in planetRepository.cache } // skip already in cache
            .flatMapMerge(concurrency = concurrency) { id ->
                flow {
                    val detailsState =
                        planetRepository.getPlanet(id).getOrNull()
                            ?.let { DetailsSuccess(it) }
                            ?: DetailsError()
                    emit(id to detailsState)
                }
            }
            .collect { (id, detailsState) ->
                if (detailsState is DetailsSuccess) {
                    planetRepository.cache[id] = detailsState.details
                }
                indexById[id]?.let { idx ->
                    currentItems[idx] = currentItems[idx].copy(detailsState = detailsState)
                    send(ListSuccess(currentItems.toList()))
                }
            }
    }.flowOn(Dispatchers.IO)
}