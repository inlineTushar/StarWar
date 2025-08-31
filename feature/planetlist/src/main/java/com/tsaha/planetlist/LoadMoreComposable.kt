package com.tsaha.planetlist

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@Composable
fun LoadMoreComposable(
    loadMoreEnable: () -> Boolean,
    onNext: () -> Unit,
    lastIndexVisibilityThreshold: Int = 1,
    listState: LazyListState
) {
    if (loadMoreEnable()) {
        val shouldLoadMore: Boolean by remember {
            derivedStateOf {
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItems = listState.layoutInfo.totalItemsCount
                val aboutToEnd = totalItems > 0
                        && lastVisibleItemIndex != null
                        && lastVisibleItemIndex >= totalItems - lastIndexVisibilityThreshold
                val isUserTriggered = listState.isScrollInProgress
                        && listState.firstVisibleItemIndex > 0
                if (!listState.canScrollForward && totalItems > 0) true
                else aboutToEnd && isUserTriggered
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) onNext()
        }
    }
}