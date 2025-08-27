package com.tsaha.nucleus.data.model

/**
 * Represents pagination information for API responses
 * @param hasNext true if there is a next page available, false otherwise
 */
data class PaginationInfo(
    val currentPage: Int,
    val nextPage: String?,
) {
    val hasNext: Boolean = nextPage != null
}