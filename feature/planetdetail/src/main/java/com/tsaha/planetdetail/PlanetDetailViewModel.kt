package com.tsaha.planetdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.PlanetDetailsUiState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class PlanetDetailViewModel(
    private val planetId: String,
    private val planetRepository: PlanetRepository
) : ViewModel() {

    private val uiStateMutable = MutableStateFlow<PlanetDetailsUiState>(DetailsLoading)
    val uiState: StateFlow<PlanetDetailsUiState> = uiStateMutable
        .onStart { getPlanetDetail(planetId) }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = DetailsLoading
        )

    suspend fun getPlanetDetail(planetId: String) {
        planetRepository.getPlanet(planetId)
            .onSuccess { planet -> uiStateMutable.value = DetailsSuccess(planet) }
            .onFailure { uiStateMutable.value = DetailsError(it.message) }
    }
}
