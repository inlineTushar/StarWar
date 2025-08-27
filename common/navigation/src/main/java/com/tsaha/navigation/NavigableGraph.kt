package com.tsaha.navigation

import kotlinx.serialization.Serializable

sealed class NavigableGraph : Navigable {
    @Serializable data object StarList : NavigableGraph()
    @Serializable
    data class StarDetails(
        val starId: String,
    ) : NavigableGraph()
}

@Serializable
data object ToBack : Navigable
