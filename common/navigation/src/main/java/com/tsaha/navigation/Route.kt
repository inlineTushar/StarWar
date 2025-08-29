package com.tsaha.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Route(val value: String) {
    @Serializable
    data object PlanetList : Route("planet/list")
    @Serializable
    data object PlanetDetails : Route("planet/details/{planetId}") {
        fun create(planetId: String) = "planet/details/$planetId"
    }
}