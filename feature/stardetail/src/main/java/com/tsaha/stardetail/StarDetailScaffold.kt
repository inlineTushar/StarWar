package com.tsaha.stardetail

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
import androidx.compose.ui.tooling.preview.Preview
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.navigation.PreviousScreen
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.theme.NucleusTheme

@Composable
fun StarDetailScaffold(
    modifier: Modifier = Modifier,
    onNavigate: OnNavigateTo,
) {
    Scaffold(
        topBar = {
            NucleusAppBar(
                title = "Star Detail",
                isBackVisible = true,
                onBack = { onNavigate(PreviousScreen, {}) })
        },
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Button(onClick = { onNavigate(PreviousScreen, {}) }) {
                Text(text = "Star Details")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarDetailScaffoldPreview() {
    NucleusTheme {
        StarDetailScaffold(onNavigate = { _, _ -> })
    }
}