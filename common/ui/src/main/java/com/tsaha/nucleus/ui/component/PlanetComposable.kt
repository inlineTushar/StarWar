package com.tsaha.nucleus.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlanetComposable(
    headlineContent: @Composable () -> Unit,
    subHeadingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = subHeadingContent,
        modifier = modifier.clickable { onClick() },
    )
}

@Composable
fun PlanetNameComposable(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier.fillMaxWidth()
    )
}