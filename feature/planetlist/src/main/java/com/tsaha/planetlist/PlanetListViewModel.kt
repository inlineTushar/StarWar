package com.tsaha.planetlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.planetlist.NavEvent.*
import com.tsaha.planetlist.mapper.toUiState
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class PlanetListViewModel(
    private val planetListUseCase: PlanetListUseCase
) : ViewModel() {
    private val navEventChannel = Channel<NavEvent>(Channel.BUFFERED)
    val navEvent = navEventChannel.receiveAsFlow()

    private val nextPageCounterMutableFlow = MutableSharedFlow<Int>(replay = 1)
    val nextPageCounterFlow = nextPageCounterMutableFlow.asSharedFlow()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    val uiState: StateFlow<PlanetListUiState> =
        searchQuery
            .debounce(300) // Debounce search input for 300ms
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    _isSearchMode.value = false
                    planetListUseCase.observePlanets(nextPageCounterFlow)
                } else {
                    _isSearchMode.value = true
                    planetListUseCase.searchPlanets(query)
                }
            }
            .map { planetListResult -> planetListResult.toUiState() }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = ListLoading
            )

    fun onClickPlanet(planet: Planet) {
        viewModelScope.launch {
            navEventChannel.send(ToPlanetDetails(planet.uid))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun onRequestInitialOrNextPage() {
        if (!_isSearchMode.value) {
            val currentPage = nextPageCounterMutableFlow.replayCache.firstOrNull() ?: 0
            nextPageCounterMutableFlow.tryEmit(currentPage + 1)
        }
    }

    init {
        onRequestInitialOrNextPage()
    }
}

sealed interface NavEvent {
    data class ToPlanetDetails(val uid: String) : NavEvent
}