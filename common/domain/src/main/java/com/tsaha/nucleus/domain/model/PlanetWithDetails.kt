package com.tsaha.nucleus.domain.model

import com.tsaha.nucleus.data.model.Planet

/**
 * Domain model representing a planet with its details fetch state.
 * This separates business logic concerns from UI state management.
 */
data class PlanetWithDetails(
    val planet: Planet,
    val detailsState: PlanetDetailsState
)

/**
 * Domain-specific state representing the status of fetching planet details.
 * Unlike UI state, this focuses on the data availability status.
 */
sealed class PlanetDetailsState {
    /**
     * Details are being fetched
     */
    data object Loading : PlanetDetailsState()

    /**
     * Details fetch failed
     * @param message Optional error message
     */
    data class Error(val message: String? = null) : PlanetDetailsState()

    /**
     * Details successfully fetched
     * @param climate Planet's climate information
     * @param population Planet's population
     * @param diameter Planet's diameter
     * @param gravity Planet's gravity
     * @param terrain Planet's terrain type
     */
    data class Available(
        val climate: String,
        val population: String,
        val diameter: String,
        val gravity: String,
        val terrain: String
    ) : PlanetDetailsState()
}
