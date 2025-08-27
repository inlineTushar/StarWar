package com.tsaha.stardetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.tsaha.feature.stardetail.R
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.navigation.ToBack
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.theme.NucleusTheme

@Composable
fun StarDetailScreen(
    starId: String,
    modifier: Modifier = Modifier,
    onNavigate: OnNavigateTo,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            NucleusAppBar(
                title = stringResource(id = R.string.feature_stardetail_title),
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
                Text(text = "Star Details $starId")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarDetailScreenPreview() {
    NucleusTheme {
        StarDetailScreen(starId = "XB123", onNavigate = { _, _ -> })
    }
}