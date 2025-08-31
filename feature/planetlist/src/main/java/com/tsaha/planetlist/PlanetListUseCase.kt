package com.tsaha.planetlist

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
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest

class PlanetListUseCase(
    private val planetRepository: PlanetRepository
) {
    fun observePlanets(pageSize: Int = PAGE_SIZE): Flow<PlanetListUiState> = flow {
        emit(ListLoading)

        val planetsResult = planetRepository.getPlanetsWithPagination(pageSize)
        val planets: List<Planet> = planetsResult.getOrNull()?.second.orEmpty()

        if (planets.isEmpty()) {
            val msg = planetsResult.exceptionOrNull()?.message
            emit(ListError(msg))
            return@flow
        }

        val currentItems = planets.map { planet ->
            PlanetItem(
                planet = planet,
                detailsState = DetailsLoading
            )
        }.toMutableList()

        emit(ListSuccess(currentItems.toList()))

        val indexByPlanetId = planets.mapIndexed { idx, planet -> planet.uid to idx }.toMap()

        planets.asFlow()
            .flatMapMerge { planet ->
                flow {
                    val planetDetails =
                        planetRepository.getPlanet(planet.uid).getOrNull()
                            ?.let { DetailsSuccess(it) }
                            ?: DetailsError()
                    emit(planet to planetDetails)
                }
            }
            .collect { (planet, planetDetails) ->
                indexByPlanetId[planet.uid]?.let { idx ->
                    if (currentItems[idx].detailsState is DetailsLoading) {
                        currentItems[idx] = currentItems[idx].copy(detailsState = planetDetails)
                        emit(ListSuccess(currentItems.toList()))
                    }
                }
            }
    }.flowOn(Dispatchers.IO)
}