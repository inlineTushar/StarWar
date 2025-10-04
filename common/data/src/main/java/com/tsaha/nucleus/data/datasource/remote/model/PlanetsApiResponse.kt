package com.tsaha.nucleus.data.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanetsApiResponse(
    val message: String,
    val next: String?,
    val results: List<PlanetApiModel>
)