package com.tsaha.planetdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class PlanetDetailViewModel(
    private val planetRepository: PlanetRepository
) : ViewModel() {

    private val uiStateMutable = MutableStateFlow<PlanetDetailsUiState>(DetailsLoading)
    val uiState: StateFlow<PlanetDetailsUiState> = uiStateMutable.asStateFlow()

    suspend fun getPlanetDetail(planetId: String) {
        channelFlow {
            send(DetailsLoading)
            val cache = planetRepository.cache[planetId]
            if (cache != null) {
                send(PlanetDetailsUiState.DetailsSuccess(cache))
            } else {
                planetRepository.getPlanet(planetId)
                    .onSuccess { planet -> send(PlanetDetailsUiState.DetailsSuccess(planet)) }
                    .onFailure { send(PlanetDetailsUiState.DetailsError(it.message)) }
            }
        }.onEach { uiStateMutable.value = it }.stateIn(viewModelScope)
    }
}
