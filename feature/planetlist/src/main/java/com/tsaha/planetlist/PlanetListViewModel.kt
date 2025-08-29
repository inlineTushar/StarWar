package com.tsaha.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlanetListViewModel(
    planetListUseCase: PlanetListUseCase
) : ViewModel() {
    val uiState: StateFlow<PlanetListUiState> = planetListUseCase.observePlanets()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = ListLoading
        )

    private val navEventMutable = MutableSharedFlow<NavEvent>()
    val navEvent: SharedFlow<NavEvent> = navEventMutable

    fun onClickPlanet(planet: Planet) {
        viewModelScope.launch {
            navEventMutable.emit(NavEvent.ToPlanetDetails(planet.uid))
        }
    }
}

sealed interface NavEvent {
    data class ToPlanetDetails(val uid: String) : NavEvent
}