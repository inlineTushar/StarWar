package com.tsaha.planetlist.model

import com.tsaha.nucleus.data.model.Planet

sealed class PlanetListUiState {
    data object ListLoading : PlanetListUiState()
    data class ListError(val errorMessage: String? = null) : PlanetListUiState()
    data class ListSuccess(val planetItems: List<PlanetItem>) : PlanetListUiState()
}

data class PlanetItem(
    val planet: Planet,
    val loadingState: PlanetItemLoadingState = PlanetItemLoadingState.Loading
)

sealed class PlanetItemLoadingState {
    data object Loading : PlanetItemLoadingState()
    data class Loaded(
        val climate: String,
        val population: String
    ) : PlanetItemLoadingState()

    data class LoadFailed(val errorMessage: String? = null) : PlanetItemLoadingState()
}
