package com.tsaha.nucleus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Planet(
    val uid: String,
    val name: String,
    val url: String
)