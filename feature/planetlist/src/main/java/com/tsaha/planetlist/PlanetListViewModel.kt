package com.tsaha.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class PlanetListViewModel(
    planetListUseCase: PlanetListUiUseCase
) : ViewModel() {
    val uiState = planetListUseCase.observePlanets()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = ListLoading
        )
}
