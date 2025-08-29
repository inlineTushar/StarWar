package com.tsaha.planetlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tsaha.feature.planetlist.R
import com.tsaha.navigation.NavigableGraph.PlanetDetailsNavigable
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsSuccess
import com.tsaha.nucleus.ui.component.ErrorComposable
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.component.PlanetComposable
import com.tsaha.nucleus.ui.component.PlanetNameComposable
import com.tsaha.nucleus.ui.component.ProgressBarComposable
import com.tsaha.nucleus.ui.component.ShimmerComposable
import com.tsaha.nucleus.ui.theme.NucleusTheme
import com.tsaha.planetlist.model.PlanetItem
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListError
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import com.tsaha.planetlist.model.PlanetListUiState.ListSuccess
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlanetListScreen(
    modifier: Modifier = Modifier,
    viewModel: PlanetListViewModel = koinViewModel(),
    onNavigate: OnNavigateTo,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PlanetListComposable(
        state = state,
        modifier = modifier,
        onNavigate = onNavigate
    )
}

@Composable
private fun PlanetListComposable(
    state: PlanetListUiState,
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
    ) { padding ->
        when (val current = state) {
            ListLoading -> ProgressBarComposable(modifier = Modifier.padding(padding))

            is ListSuccess ->
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    items(
                        items = current.planetItems,
                        key = { it.planet.uid },
                    ) { item ->
                        PlanetComposable(
                            headlineContent = {
                                PlanetNameComposable(
                                    name = stringResource(
                                        com.tsaha.nucleus.ui.R.string.common_ui_planet_name,
                                        item.planet.name
                                    )
                                )
                            },
                            subHeadingContent = { PlanetInfoComposable(planetDetailsUiState = item.detailsState) },
                            onClick = { onNavigate(PlanetDetailsNavigable(planetId = item.planet.uid)) {} }
                        )
                    }
                }

            is ListError ->
                ErrorComposable(
                    errorText = stringResource(R.string.feature_planetlist_loading_error),
                    modifier = Modifier.padding(padding)
                )
        }
    }
}

@Composable
private fun PlanetInfoComposable(
    planetDetailsUiState: PlanetDetailsUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        when (planetDetailsUiState) {
            is DetailsLoading -> {
                ShimmerComposable()
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                ShimmerComposable()
            }

            is DetailsSuccess -> {
                Text(
                    text = stringResource(
                        com.tsaha.nucleus.ui.R.string.common_ui_planet_climate,
                        planetDetailsUiState.details.climate
                    ),
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = stringResource(
                        com.tsaha.nucleus.ui.R.string.common_ui_planet_population,
                        planetDetailsUiState.details.population
                    ),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            else -> {}
        }
    }
}

@Composable
@Preview(showBackground = false)
private fun PlanetListItemPreview() {
    NucleusTheme {
        PlanetComposable(
            headlineContent = { Text(text = "Earth") },
            subHeadingContent = { PlanetInfoComposable(DetailsLoading) },
            onClick = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun PlanetListComposableLoadingPreview() {
    NucleusTheme {
        PlanetListComposable(
            state = ListLoading,
            onNavigate = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetListComposableErrorPreview() {
    NucleusTheme {
        PlanetListComposable(
            state = ListError(),
            onNavigate = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetListComposableSuccessPreview() {
    NucleusTheme {
        PlanetListComposable(
            state = ListSuccess(
                planetItems = listOf(
                    PlanetItem(
                        planet = Planet(
                            uid = "1",
                            name = "Earth"
                        ),
                        detailsState = DetailsLoading
                    ),
                    PlanetItem(
                        planet = Planet(
                            uid = "2",
                            name = "Mars"
                        ),
                        detailsState = DetailsSuccess(
                            details = PlanetDetails(
                                uid = "2",
                                name = "Mars",
                                climate = "Cold",
                                population = "1M",
                                diameter = "1000km",
                                gravity = "1g",
                                terrain = "Rocky"
                            )
                        )
                    )
                )
            ),
            onNavigate = { _, _ -> }
        )
    }
}
