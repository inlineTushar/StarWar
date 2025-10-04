package com.tsaha.planetlist

import com.tsaha.nucleus.core.network.PAGE_SIZE
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.planetlist.domain.model.PlanetDetailsState
import com.tsaha.planetlist.domain.model.PlanetListResult
import com.tsaha.planetlist.domain.model.PlanetWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Domain use case for planet list operations.
 * This class is UI-agnostic and returns domain-specific models.
 * The ViewModel layer is responsible for mapping these to UI states.
 */
class PlanetListUseCase(
    private val planetRepository: PlanetRepository
) {
    /**
     * Observes planet list with pagination support.
     *
     * @param loadNextFlow Flow emitting page numbers to load
     * @param pageSize Number of items to fetch details for per page
     * @return Flow of domain-specific planet list results
     */
    fun observePlanets(
        loadNextFlow: Flow<Int>,
        pageSize: Int = PAGE_SIZE
    ): Flow<PlanetListResult> = flow {
        emit(PlanetListResult.Loading)
        var hasNext = true
        var currentPlanets = mutableListOf<Planet>()
        var currentPlanetItems = mutableListOf<PlanetWithDetails>()

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
                            emit(PlanetListResult.Error(msg))
                            return@flow
                        }

                        currentPlanetItems = currentPlanets.map { planet ->
                            PlanetWithDetails(
                                planet = planet,
                                detailsState = PlanetDetailsState.Loading
                            )
                        }.toMutableList()
                    } else {
                        emit(
                            PlanetListResult.Success(
                                items = currentPlanetItems.toList(),
                                isLoadingNextPage = true
                            )
                        )
                        val planetsResult =
                            planetRepository.getPlanetsWithPagination(pageNumber = pageNumber)
                        hasNext = planetsResult.getOrNull()?.first?.hasNext ?: true
                        val newPlanets = planetsResult.getOrNull()?.second.orEmpty().toMutableList()

                        if (newPlanets.isNotEmpty()) {
                            currentPlanets.addAll(newPlanets)
                            val newPlanetItems = newPlanets.map { planet ->
                                PlanetWithDetails(
                                    planet = planet,
                                    detailsState = PlanetDetailsState.Loading
                                )
                            }.toMutableList()
                            currentPlanetItems.addAll(newPlanetItems)
                        }
                    }
                    emit(
                        PlanetListResult.Success(
                            items = currentPlanetItems.toList(),
                            isLoadingNextPage = false
                        )
                    )
                }
            }.collect { result ->
                if (result is PlanetListResult.Success) {
                    val indexByPlanetId =
                        currentPlanets
                            .toList()
                            .mapIndexed { idx, planet -> planet.uid to idx }.toMap()

                    result
                        .items
                        .takeLast(pageSize)
                        .asFlow()
                        .flatMapMerge { item ->
                            flow {
                                val detailsState = planetRepository.getPlanet(item.planet.uid)
                                    .fold(
                                        onSuccess = { details ->
                                            PlanetDetailsState.Available(
                                                climate = details.climate,
                                                population = details.population,
                                                diameter = details.diameter,
                                                gravity = details.gravity,
                                                terrain = details.terrain
                                            )
                                        },
                                        onFailure = { exception ->
                                            PlanetDetailsState.Error(exception.message)
                                        }
                                    )
                                emit(item to detailsState)
                            }
                        }
                        .collect { (item, detailsState) ->
                            indexByPlanetId[item.planet.uid]?.let { idx ->
                                if (currentPlanetItems[idx].detailsState is PlanetDetailsState.Loading) {
                                    currentPlanetItems[idx] =
                                        currentPlanetItems[idx].copy(detailsState = detailsState)
                                    emit(
                                        PlanetListResult.Success(
                                            items = currentPlanetItems.toList(),
                                            isLoadingNextPage = false
                                        )
                                    )
                                } else emit(result)
                            }
                        }
                } else emit(result)
            }
    }.flowOn(Dispatchers.IO)

    /**
     * Searches planets by name.
     *
     * @param query Search query string
     * @return Flow of domain-specific search results
     */
    fun searchPlanets(query: String): Flow<PlanetListResult> = flow {
        emit(
            PlanetListResult.SearchResult(
                items = emptyList(),
                query = query,
                isSearching = true
            )
        )

        if (query.isBlank()) {
            emit(
                PlanetListResult.SearchResult(
                    items = emptyList(),
                    query = query,
                    isSearching = false
                )
            )
            return@flow
        }

        val searchResult = planetRepository.searchPlanets(query)
        searchResult.fold(
            onSuccess = { planetDetailsList ->
                val searchItems = planetDetailsList.map { details ->
                    val planet = Planet(
                        uid = details.uid,
                        name = details.name
                    )
                    PlanetWithDetails(
                        planet = planet,
                        detailsState = PlanetDetailsState.Available(
                            climate = details.climate,
                            population = details.population,
                            diameter = details.diameter,
                            gravity = details.gravity,
                            terrain = details.terrain
                        )
                    )
                }
                emit(
                    PlanetListResult.SearchResult(
                        items = searchItems,
                        query = query,
                        isSearching = false
                    )
                )
            },
            onFailure = { exception ->
                emit(PlanetListResult.Error(exception.message))
            }
        )
    }.flowOn(Dispatchers.IO)
}