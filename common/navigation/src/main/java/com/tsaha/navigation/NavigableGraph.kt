package com.tsaha.navigation

import kotlinx.serialization.Serializable

sealed class NavigableGraph : Navigable {
    @Serializable
    data object PlanetListNavigable : NavigableGraph()
    @Serializable
    data class PlanetDetailsNavigable(
        val planetId: String,
    ) : NavigableGraph()
}

@Serializable
data object ToBack : Navigable
