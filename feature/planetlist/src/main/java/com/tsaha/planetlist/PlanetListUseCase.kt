package com.tsaha.planetlist

import com.tsaha.nucleus.core.network.PAGE_SIZE
import com.tsaha.nucleus.data.model.PlanetApiModel
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsError
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsSuccess
import com.tsaha.planetlist.model.PlanetItem
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListError
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import com.tsaha.planetlist.model.PlanetListUiState.ListSuccess
import com.tsaha.planetlist.model.PlanetListUiState.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlanetListUseCase(
    private val planetRepository: PlanetRepository
) {
    fun observePlanets(
        loadNextFlow: Flow<Int>,
        pageSize: Int = PAGE_SIZE
    ): Flow<PlanetListUiState> = flow {
        emit(ListLoading)
        var hasNext = true
        var currentPlanets = mutableListOf<PlanetApiModel>()
        var currentPlanetItems = mutableListOf<PlanetItem>()

        loadNextFlow
            .buffer(capacity = 0, onBufferOverflow = BufferOverflow.DROP_LATEST)
            .flatMapMerge(concurrency = 1) { pageNumber ->
                flow {
                    if (!hasNext) return@flow
                    if (pageNumber == 1) {
                        val planetsResult =
                            planetRepository.getPlanetsWithPagination(pageNumber = pageNumber)
                        hasNext = planetsResult.getOrNull()?.first?.hasNext ?: true
                        currentPlanets = planetsResult.getOrNull()?.second.orEmpty().toMutableList()

                        if (currentPlanets.isEmpty()) {
                            val msg = planetsResult.exceptionOrNull()?.message
                            emit(ListError(msg))
                            return@flow
                        }

                        currentPlanetItems = currentPlanets.map { planet ->
                            PlanetItem(
                                planet = planet,
                                detailsState = DetailsLoading
                            )
                        }.toMutableList()
                    } else {
                        emit(
                            ListSuccess(
                                isPageLoading = true,
                                planetItems = currentPlanetItems.toList()
                            )
                        )
                        val planetsResult =
                            planetRepository.getPlanetsWithPagination(pageNumber = pageNumber)
                        hasNext = planetsResult.getOrNull()?.first?.hasNext ?: true
                        val newPlanets = planetsResult.getOrNull()?.second.orEmpty().toMutableList()

                        if (newPlanets.isNotEmpty()) {
                            currentPlanets.addAll(newPlanets)
                            val newPlanetItems = newPlanets.map { planet ->
                                PlanetItem(
                                    planet = planet,
                                    detailsState = DetailsLoading
                                )
                            }.toMutableList()
                            currentPlanetItems.addAll(newPlanetItems)
                        }
                    }
                    emit(
                        ListSuccess(
                            isPageLoading = false,
                            planetItems = currentPlanetItems.toList()
                        )
                    )
                }
            }.collect { uiState ->
                if (uiState is ListSuccess) {
                    val indexByPlanetItemId =
                        currentPlanets
                            .toList()
                            .mapIndexed { idx, planet -> planet.uid to idx }.toMap()

                    uiState
                        .planetItems
                        .takeLast(pageSize)
                        .asFlow()
                        .flatMapMerge { item ->
                            flow {
                                val planetDetails =
                                    planetRepository.getPlanet(item.planet.uid).getOrNull()
                                        ?.let { DetailsSuccess(it) }
                                        ?: DetailsError()
                                emit(item to planetDetails)
                            }
                        }
                        .collect { (item, planetDetails) ->
                            indexByPlanetItemId[item.planet.uid]?.let { idx ->
                                if (currentPlanetItems[idx].detailsState is DetailsLoading) {
                                    currentPlanetItems[idx] =
                                        currentPlanetItems[idx].copy(detailsState = planetDetails)
                                    emit(ListSuccess(planetItems = currentPlanetItems.toList()))
                                } else emit(uiState)
                            }
                        }
                } else emit(uiState)
            }
    }.flowOn(Dispatchers.IO)

    fun searchPlanets(query: String): Flow<PlanetListUiState> = flow {
        emit(SearchResult(planetItems = emptyList(), searchQuery = query, isSearching = true))

        if (query.isBlank()) {
            emit(SearchResult(planetItems = emptyList(), searchQuery = query, isSearching = false))
            return@flow
        }

        val searchResult = planetRepository.searchPlanets(query)
        searchResult.fold(
            onSuccess = { planetDetailsList ->
                val searchItems = planetDetailsList.map { planetDetails ->
                    // Convert PlanetDetails to Planet for consistency
                    val planet = PlanetApiModel(
                        uid = planetDetails.uid,
                        name = planetDetails.name
                    )
                    PlanetItem(
                        planet = planet,
                        detailsState = DetailsSuccess(planetDetails)
                    )
                }
                emit(
                    SearchResult(
                        planetItems = searchItems,
                        searchQuery = query,
                        isSearching = false
                    )
                )
            },
            onFailure = { exception ->
                emit(ListError(exception.message))
            }
        )
    }.flowOn(Dispatchers.IO)
}