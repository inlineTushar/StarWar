package com.tsaha.nucleus.data.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanetApiModel(
    val uid: String,
    val name: String
)