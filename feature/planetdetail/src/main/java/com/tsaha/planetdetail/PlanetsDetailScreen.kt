package com.tsaha.planetdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.ParagraphStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tsaha.feature.planetdetail.R
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsError
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsLoading
import com.tsaha.nucleus.ui.PlanetDetailsUiState.DetailsSuccess
import com.tsaha.nucleus.ui.component.ErrorComposable
import com.tsaha.nucleus.ui.component.NucleusAppBar
import androidx.compose.foundation.layout.height
import com.tsaha.nucleus.ui.component.PlanetComposable
import com.tsaha.nucleus.ui.component.PlanetNameComposable
import com.tsaha.nucleus.ui.component.ProgressBarComposable
import com.tsaha.nucleus.ui.theme.NucleusTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import com.tsaha.nucleus.ui.R as CommonR

@Composable
fun PlanetDetailsScreen(
    planetId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    vm: PlanetDetailViewModel = koinViewModel { parametersOf(planetId) },
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    PlanetDetailsComposable(
        state = state,
        onClickBack = { navController.popBackStack() },
        modifier = modifier
    )
}

@Composable
private fun PlanetDetailsComposable(
    state: PlanetDetailsUiState,
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NucleusAppBar(
                title = stringResource(id = R.string.feature_planetdetail_title),
                isBackVisible = true,
                onBack = onClickBack
            )
        },
    ) { padding ->
        when (val current = state) {
            is DetailsError -> {
                ErrorComposable(
                    errorText = stringResource(id = R.string.feature_planetdetail_loading_error),
                    modifier = Modifier.padding(padding)
                )
            }

            DetailsLoading -> ProgressBarComposable(modifier = Modifier.padding(padding))

            is DetailsSuccess -> {
                PlanetComposable(
                    headlineContent = {
                        PlanetNameComposable(
                            name = current.details.name,
                            stylable = true
                        )
                    },
                    subHeadingContent = {
                        PlanetInfoComposable(
                            planet = current.details,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    },
                    label = stringResource(
                        CommonR.string.common_ui_accessibility_planet_details,
                        current.details.name
                    ),
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun PlanetInfoComposable(
    planet: PlanetDetails,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
        ) {
            PlanetInfoUnitComposable(
                title = stringResource(CommonR.string.common_ui_planet_climate),
                value = planet.climate
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanetInfoUnitComposable(
                title = stringResource(CommonR.string.common_ui_planet_population),
                value = planet.population
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanetInfoUnitComposable(
                title = stringResource(CommonR.string.common_ui_planet_diameter),
                value = planet.diameter
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanetInfoUnitComposable(
                title = stringResource(CommonR.string.common_ui_planet_gravity),
                value = planet.gravity
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanetInfoUnitComposable(
                title = stringResource(CommonR.string.common_ui_planet_terrain),
                value = planet.terrain
            )
        }
    }
}

@Composable
fun PlanetInfoUnitComposable(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = ParagraphStyle(lineHeight = 28.sp)
            ) {
                withStyle(SpanStyle(fontWeight = FontWeight.Thin)) {
                    append("$title:\n")
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Medium, letterSpacing = 5.sp)) {
                    append(value)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PlanetDetailScreenPreview() {
    NucleusTheme {
        PlanetDetailsComposable(
            state = DetailsSuccess(
                details = PlanetDetails(
                    uid = "1",
                    name = "Tatooine",
                    climate = "Arid",
                    population = "200000",
                    diameter = "10465",
                    gravity = "1 standard",
                    terrain = "Desert"
                )
            ),
            onClickBack = {},
        )
    }
}