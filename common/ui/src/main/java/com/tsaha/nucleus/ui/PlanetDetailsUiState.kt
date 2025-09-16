package com.tsaha.nucleus.ui

import com.tsaha.nucleus.data.model.PlanetDetailsApiModel

sealed class PlanetDetailsUiState {
    data object DetailsLoading : PlanetDetailsUiState()
    data class DetailsError(val errorMessage: String? = null) : PlanetDetailsUiState()
    data class DetailsSuccess(val details: PlanetDetailsApiModel) : PlanetDetailsUiState()
}