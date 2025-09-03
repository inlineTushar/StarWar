package com.tsaha.nucleus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanetDetails(
    val planet: Planet,
    val climate: String,
    val population: String,
    val diameter: String,
    val gravity: String,
    val terrain: String
) {
    val uid: String get() = planet.uid
    val name: String get() = planet.name
}
