package com.tsaha.navigation

import kotlinx.serialization.Serializable

sealed class NavigableGraph : Navigable {
    @Serializable data object StarList : NavigableGraph()
    @Serializable data object StarDetails : NavigableGraph()
}

@Serializable
data object PreviousScreen : Navigable
