package com.tsaha.nucleus.data.datasource.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PaginationApiModel(
    val currentPage: Int,
    val nextPage: String?,
)
