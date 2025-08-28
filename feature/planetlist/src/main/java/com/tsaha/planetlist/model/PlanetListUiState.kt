package com.tsaha.planetlist.model

import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.ui.PlanetDetailsUiState

sealed class PlanetListUiState {
    data object ListLoading : PlanetListUiState()
    data class ListError(val errorMessage: String? = null) : PlanetListUiState()
    data class ListSuccess(val planetItems: List<PlanetItem>) : PlanetListUiState()
}

data class PlanetItem(
    val planet: Planet,
    val detailsState: PlanetDetailsUiState = PlanetDetailsUiState.DetailsLoading
)
