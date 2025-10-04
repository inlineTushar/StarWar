package com.tsaha.nucleus.domain.model

/**
 * Domain model representing the result of planet list operations.
 * This is UI-agnostic and focuses purely on business logic state.
 */
sealed class PlanetListResult {
    /**
     * Initial loading state when fetching the first page
     */
    data object Loading : PlanetListResult()

    /**
     * Error state with optional error message
     */
    data class Error(val message: String?) : PlanetListResult()

    /**
     * Success state containing planet items with their details
     * @param items List of planet items with their detail fetch status
     * @param isLoadingNextPage Whether a pagination request is in progress
     */
    data class Success(
        val items: List<PlanetWithDetails>,
        val isLoadingNextPage: Boolean = false
    ) : PlanetListResult()

    /**
     * Search result state
     * @param items List of planet items matching the search query
     * @param query The search query used
     * @param isSearching Whether the search is in progress
     */
    data class SearchResult(
        val items: List<PlanetWithDetails>,
        val query: String,
        val isSearching: Boolean = false
    ) : PlanetListResult()
}
