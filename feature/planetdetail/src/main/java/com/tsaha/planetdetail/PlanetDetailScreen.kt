package com.tsaha.planetdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tsaha.feature.planetdetail.R
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.navigation.ToBack
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsError
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsSuccess
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.component.NucleusProgressIndicator
import com.tsaha.nucleus.ui.component.PlanetComposable
import com.tsaha.nucleus.ui.component.PlanetNameComposable
import com.tsaha.nucleus.ui.theme.NucleusTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlanetDetailScreen(
    planetId: String,
    modifier: Modifier = Modifier,
    onNavigate: OnNavigateTo,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NucleusAppBar(
                title = stringResource(id = R.string.feature_planetdetail_title),
                isBackVisible = true,
                onBack = { onNavigate(ToBack) {} }
            )
        },
    ) { contentPadding ->
        val viewModel = koinViewModel<PlanetDetailViewModel>()
        val state by viewModel.uiState.collectAsState()
        LaunchedEffect(planetId) {
            viewModel.getPlanetDetail(planetId)
        }
        when (state) {
            is DetailsError -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                ) {
                    Text(text = "Planets Couldn't be reached!")
                }
            }

            DetailsLoading -> NucleusProgressIndicator(
                modifier = Modifier.padding(contentPadding)
            )

            is DetailsSuccess -> {
                PlanetComposable(
                    headlineContent = {
                        PlanetNameComposable(name = (state as DetailsSuccess).details.name)
                    },
                    subHeadingContent = {
                        PlanetDetailsComposable(planet = (state as DetailsSuccess).details)
                    },
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }
    }
}

@Composable
private fun PlanetDetailsComposable(
    planet: PlanetDetails,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(text = planet.climate)
        Text(text = planet.population)
        Text(text = planet.diameter)
        Text(text = planet.gravity)
        Text(text = planet.terrain)
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetDetailScreenPreview() {
    NucleusTheme {
        PlanetDetailScreen(planetId = "XB123", onNavigate = { _, _ -> })
    }
}