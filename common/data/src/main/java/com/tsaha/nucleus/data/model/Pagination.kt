package com.tsaha.nucleus.data.model

data class Pagination(
    val currentPage: Int,
    val nextPage: String?,
) {
    val hasNext: Boolean = nextPage != null
}
