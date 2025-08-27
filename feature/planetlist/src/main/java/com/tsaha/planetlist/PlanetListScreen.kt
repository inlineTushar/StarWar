package com.tsaha.planetlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tsaha.feature.planetlist.R
import com.tsaha.navigation.NavigableGraph.PlanetDetails
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.component.shimmer
import com.tsaha.nucleus.ui.theme.NucleusTheme

@Composable
fun PlanetListScreen(
    modifier: Modifier = Modifier,
    onNavigate: OnNavigateTo,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            NucleusAppBar(
                title = stringResource(id = R.string.feature_planetlist_title),
                isBackVisible = false
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            items(
                items = (1..10).toList(),
                key = { it }
            ) { item ->
                PlanetListItem(
                    headlineContent = {
                        PlanetNameComposable(name = "Title$item")
                    },
                    subHeadingContent = {
                        PlanetInfoComposable(
                            infoPrimaryText = "Mass:$item",
                            infoSecondaryText = "Distance:$item",
                            isLoading = false
                        )
                    },
                    onClick = { onNavigate(PlanetDetails(planetId = "item$item")) {} }
                )
            }
        }
    }
}

@Composable
private fun PlanetListItem(
    headlineContent: @Composable () -> Unit,
    subHeadingContent: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = subHeadingContent,
        modifier = Modifier.clickable { onClick() },
    )
}

@Composable
private fun PlanetNameComposable(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PlanetInfoComposable(
    infoPrimaryText: String,
    infoSecondaryText: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
) {
    Column(modifier = modifier) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(8.dp)
                    .shimmer()
            )
        } else {
            Text(text = infoPrimaryText)
            Text(text = infoSecondaryText)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetListItemPreview() {
    NucleusTheme {
        PlanetListItem(
            headlineContent = { Text(text = "Earth") },
            subHeadingContent = {
                PlanetInfoComposable(
                    infoPrimaryText = "Habitable",
                    infoSecondaryText = "7.8 billion",
                    isLoading = false,
                )
            },
            onClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetListScreenPreview() {
    NucleusTheme {
        PlanetListScreen(onNavigate = { _, _ -> })
    }
}
