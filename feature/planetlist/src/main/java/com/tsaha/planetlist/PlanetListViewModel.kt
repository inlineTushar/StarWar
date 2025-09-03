package com.tsaha.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.nucleus.core.network.PAGE_SIZE
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.planetlist.NavEvent.*
import com.tsaha.planetlist.model.PlanetItem
import com.tsaha.planetlist.model.PlanetItemLoadingState
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListError
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import com.tsaha.planetlist.model.PlanetListUiState.ListSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.orEmpty

class PlanetListViewModel(
    private val planetRepository: PlanetRepository
) : ViewModel() {
    val uiState: StateFlow<PlanetListUiState> =
        observePlanets()
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = ListLoading
            )

    private val navEventChannel = Channel<NavEvent>(Channel.BUFFERED)
    val navEvent = navEventChannel.receiveAsFlow()

    fun onClickPlanet(planet: Planet) {
        viewModelScope.launch {
            navEventChannel.send(ToPlanetDetails(planet.uid))
        }
    }

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
                loadingState = PlanetItemLoadingState.Loading
            )
        }.toMutableList()

        emit(ListSuccess(currentItems.toList()))

        val indexByPlanetId = planets.mapIndexed { idx, planet -> planet.uid to idx }.toMap()

        planets.asFlow()
            .flatMapMerge { planet ->
                flow {
                    val planetDetailsResult = planetRepository.getPlanet(planet.uid)
                    val loadingState = planetDetailsResult.getOrNull()?.let { planetDetails ->
                        PlanetItemLoadingState.Loaded(
                            climate = planetDetails.climate,
                            population = planetDetails.population
                        )
                    } ?: PlanetItemLoadingState.LoadFailed(
                        planetDetailsResult.exceptionOrNull()?.message
                    )
                    emit(planet to loadingState)
                }
            }
            .collect { (planet, loadingState) ->
                indexByPlanetId[planet.uid]?.let { idx ->
                    if (currentItems[idx].loadingState is PlanetItemLoadingState.Loading) {
                        currentItems[idx] = currentItems[idx].copy(loadingState = loadingState)
                        emit(ListSuccess(currentItems.toList()))
                    }
                }
            }
    }.flowOn(Dispatchers.IO)
}

sealed interface NavEvent {
    data class ToPlanetDetails(val uid: String) : NavEvent
}