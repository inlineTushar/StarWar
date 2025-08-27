package com.tsaha.navigation

import kotlinx.serialization.Serializable

sealed class NavigableGraph : Navigable {
    @Serializable
    data object PlanetList : NavigableGraph()
    @Serializable
    data class PlanetDetails(
        val planetId: String,
    ) : NavigableGraph()
}

@Serializable
data object ToBack : Navigable
