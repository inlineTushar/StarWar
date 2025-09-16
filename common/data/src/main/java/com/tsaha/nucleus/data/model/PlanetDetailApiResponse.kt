package com.tsaha.nucleus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanetDetailApiResponse(
    val result: PlanetDetailApiResult
)

@Serializable
data class PlanetDetailApiResult(
    val properties: PlanetDetailApiProperties,
    val uid: String
)

@Serializable
data class PlanetDetailApiProperties(
    val name: String,
    val climate: String,
    val population: String,
    val diameter: String,
    val gravity: String,
    val terrain: String
)