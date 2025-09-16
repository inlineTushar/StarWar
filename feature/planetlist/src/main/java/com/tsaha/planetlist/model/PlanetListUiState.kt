package com.tsaha.planetlist.model

import com.tsaha.nucleus.data.model.PlanetApiModel
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.PlanetDetailsUiState.*

sealed class PlanetListUiState {
    data object ListLoading : PlanetListUiState()
    data class ListError(val errorMessage: String? = null) : PlanetListUiState()
    data class ListSuccess(
        val planetItems: List<PlanetItem>,
        val isPageLoading: Boolean = false
    ) : PlanetListUiState()

    data class SearchResult(
        val planetItems: List<PlanetItem>,
        val searchQuery: String,
        val isSearching: Boolean = false
    ) : PlanetListUiState()
}

data class PlanetItem(
    val planet: PlanetApiModel,
    val detailsState: PlanetDetailsUiState = DetailsLoading
)
