package com.tsaha.planetdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tsaha.feature.planetdetail.R
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.navigation.ToBack
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.theme.NucleusTheme

@Composable
fun PlanetDetailScreen(
    planetId: String,
    modifier: Modifier = Modifier,
    onNavigate: OnNavigateTo,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            NucleusAppBar(
                title = stringResource(id = R.string.feature_planetdetail_title),
                isBackVisible = true,
                onBack = { onNavigate(ToBack) {} }
            )
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Button(onClick = {}) {
                Text(text = "Planet Details $planetId")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanetDetailScreenPreview() {
    NucleusTheme {
        PlanetDetailScreen(planetId = "XB123", onNavigate = { _, _ -> })
    }
}