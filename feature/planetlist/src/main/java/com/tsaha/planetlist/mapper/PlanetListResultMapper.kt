package com.tsaha.planetlist.mapper

import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.domain.model.PlanetDetailsState
import com.tsaha.nucleus.domain.model.PlanetListResult
import com.tsaha.nucleus.domain.model.PlanetWithDetails
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.planetlist.model.PlanetItem
import com.tsaha.planetlist.model.PlanetListUiState

/**
 * Maps domain-specific PlanetListResult to UI-specific PlanetListUiState.
 * This conversion happens in the ViewModel layer.
 */
fun PlanetListResult.toUiState(): PlanetListUiState {
    return when (this) {
        is PlanetListResult.Loading -> PlanetListUiState.ListLoading

        is PlanetListResult.Error -> PlanetListUiState.ListError(
            errorMessage = this.message
        )

        is PlanetListResult.Success -> PlanetListUiState.ListSuccess(
            planetItems = this.items.map { it.toPlanetItem() },
            isPageLoading = this.isLoadingNextPage
        )

        is PlanetListResult.SearchResult -> PlanetListUiState.SearchResult(
            planetItems = this.items.map { it.toPlanetItem() },
            searchQuery = this.query,
            isSearching = this.isSearching
        )
    }
}

/**
 * Converts domain PlanetWithDetails to UI PlanetItem.
 */
private fun PlanetWithDetails.toPlanetItem(): PlanetItem {
    return PlanetItem(
        planet = this.planet,
        detailsState = when (val state = this.detailsState) {
            is PlanetDetailsState.Loading -> PlanetDetailsUiState.DetailsLoading

            is PlanetDetailsState.Error -> PlanetDetailsUiState.DetailsError(
                errorMessage = state.message
            )

            is PlanetDetailsState.Available -> PlanetDetailsUiState.DetailsSuccess(
                details = PlanetDetails(
                    uid = this.planet.uid,
                    name = this.planet.name,
                    climate = state.climate,
                    population = state.population,
                    diameter = state.diameter,
                    gravity = state.gravity,
                    terrain = state.terrain
                )
            )
        }
    )
}

/**
 * Converts domain PlanetWithDetails to UI PlanetDetailsUiState with planet context.
 * This version includes the planet's uid and name from the parent PlanetWithDetails.
 */
fun PlanetWithDetails.toUiState(): PlanetDetailsUiState {
    return when (val state = this.detailsState) {
        is PlanetDetailsState.Loading -> PlanetDetailsUiState.DetailsLoading

        is PlanetDetailsState.Error -> PlanetDetailsUiState.DetailsError(
            errorMessage = state.message
        )

        is PlanetDetailsState.Available -> PlanetDetailsUiState.DetailsSuccess(
            details = PlanetDetails(
                uid = this.planet.uid,
                name = this.planet.name,
                climate = state.climate,
                population = state.population,
                diameter = state.diameter,
                gravity = state.gravity,
                terrain = state.terrain
            )
        )
    }
}
