package com.tsaha.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PlanetListViewModel(
    planetListUseCase: PlanetListUiUseCase
) : ViewModel() {
    val uiState: StateFlow<PlanetListUiState> = planetListUseCase.observePlanets()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = ListLoading
        )
}
