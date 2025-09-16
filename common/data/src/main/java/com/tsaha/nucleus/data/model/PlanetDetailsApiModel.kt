package com.tsaha.nucleus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanetDetailsApiModel(
    val uid: String,
    val name: String,
    val climate: String,
    val population: String,
    val diameter: String,
    val gravity: String,
    val terrain: String
)

@Serializable
data class PlanetDetailsMemoryModel(
    val uid: String,
    val name: String,
    val climate: String,
    val population: String,
    val diameter: String,
    val gravity: String,
    val terrain: String
)

fun PlanetDetailsApiModel.asMemoryModel() =
    PlanetDetailsMemoryModel(
        uid = uid,
        name = name,
        climate = climate,
        population = population,
        diameter = diameter,
        gravity = gravity,
        terrain = terrain
    )

fun PlanetDetailsMemoryModel.asApiModel() =
    PlanetDetailsApiModel(
        uid = uid,
        name = name,
        climate = climate,
        population = population,
        diameter = diameter,
        gravity = gravity,
        terrain = terrain
    )