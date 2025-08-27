package com.tsaha.nucleus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanetDetailApiResponse(
    val result: PlanetDetailResult
)

@Serializable
data class PlanetDetailResult(
    val properties: PlanetDetailProperties,
    val uid: String
)

@Serializable
data class PlanetDetailProperties(
    val name: String,
    val climate: String,
    val population: String,
    val diameter: String,
    val gravity: String,
    val terrain: String
)