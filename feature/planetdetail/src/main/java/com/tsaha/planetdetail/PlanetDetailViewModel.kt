package com.tsaha.planetdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class PlanetDetailViewModel(
    private val planetRepository: PlanetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlanetDetailsUiState>(DetailsLoading)
    val uiState: StateFlow<PlanetDetailsUiState> = _uiState.asStateFlow()

    suspend fun getPlanetDetail(planetId: String) {
        channelFlow {
            send(DetailsLoading)
            val cache = planetRepository.cache[planetId]
            if (cache != null) {
                send(PlanetDetailsUiState.DetailsSuccess(cache))
            } else {
                planetRepository.getPlanet(planetId)
                    .onSuccess { planetRepository.cache[planetId] = it }
                    .onSuccess { planet -> send(PlanetDetailsUiState.DetailsSuccess(planet)) }
                    .onFailure { send(PlanetDetailsUiState.DetailsError(it.message)) }
            }
        }.onEach { _uiState.value = it }.stateIn(viewModelScope)
    }
}
