package com.tsaha.planetlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tsaha.feature.planetlist.R
import com.tsaha.navigation.NavigableGraph.PlanetDetails
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.component.NucleusProgressIndicator
import com.tsaha.nucleus.ui.component.shimmer
import com.tsaha.nucleus.ui.theme.NucleusTheme
import com.tsaha.planetlist.model.PlanetDetailsUiState
import com.tsaha.planetlist.model.PlanetDetailsUiState.DetailsLoading
import com.tsaha.planetlist.model.PlanetDetailsUiState.DetailsSuccess
import com.tsaha.planetlist.model.PlanetListUiState.ListError
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import com.tsaha.planetlist.model.PlanetListUiState.ListSuccess
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.zip
import org.koin.compose.koinInject

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
        val viewModel = koinInject<PlanetListViewModel>()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val listState = rememberLazyListState()

        when (state) {
            is ListError -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    Text(text = "Planets Couldn't be reached!")
                }
            }

            ListLoading -> NucleusProgressIndicator(
                modifier = Modifier.padding(contentPadding)
            )

            is ListSuccess -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    items(
                        items = (state as ListSuccess).planetItems,
                        key = { planetItem -> planetItem.planet.uid }
                    ) { planetItem ->
                        PlanetListItem(
                            headlineContent = {
                                PlanetNameComposable(name = planetItem.planet.name)
                            },
                            subHeadingContent = {
                                PlanetInfoComposable(planetDetailsUiState = planetItem.detailsState)
                            },
                            onClick = { onNavigate(PlanetDetails(planetId = planetItem.planet.uid)) {} }
                        )
                    }
                }
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
    planetDetailsUiState: PlanetDetailsUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (planetDetailsUiState) {
            is DetailsLoading -> {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(8.dp)
                        .shimmer()
                )
            }

            is DetailsSuccess -> {
                Text(text = planetDetailsUiState.details.climate)
                Text(text = planetDetailsUiState.details.population)
            }

            else -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetListItemPreview() {
    NucleusTheme {
        PlanetListItem(
            headlineContent = { Text(text = "Earth") },
            subHeadingContent = { PlanetInfoComposable(DetailsLoading) },
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
