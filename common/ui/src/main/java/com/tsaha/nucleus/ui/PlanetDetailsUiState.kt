package com.tsaha.nucleus.ui

import com.tsaha.nucleus.data.model.PlanetDetails

sealed class PlanetDetailsUiState {
    data object DetailsLoading : PlanetDetailsUiState()
    data class DetailsError(val errorMessage: String? = null) : PlanetDetailsUiState()
    data class DetailsSuccess(val details: PlanetDetails) : PlanetDetailsUiState()
}