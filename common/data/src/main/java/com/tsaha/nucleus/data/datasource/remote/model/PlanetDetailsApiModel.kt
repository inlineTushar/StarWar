package com.tsaha.nucleus.data.datasource.remote.model

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