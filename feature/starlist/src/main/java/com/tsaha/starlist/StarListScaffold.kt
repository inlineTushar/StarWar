package com.tsaha.starlist

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
import com.tsaha.navigation.NavigableGraph
import com.tsaha.navigation.OnNavigateTo
import com.tsaha.nucleus.ui.component.NucleusAppBar
import com.tsaha.nucleus.ui.theme.NucleusTheme

@Composable
fun StarListScaffold(
    modifier: Modifier = Modifier,
    onNavigate: OnNavigateTo,
) {
    Scaffold(
        topBar = {
            NucleusAppBar(
                title = "Star World",
                isBackVisible = false,
                onBack = { onNavigate(NavigableGraph.StarDetails) {} })
        },
        modifier = modifier.fillMaxSize()
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Button(onClick = { onNavigate(NavigableGraph.StarDetails) {} }) {
                Text(text = "Star List")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarListScaffoldPreview() {
    NucleusTheme {
        StarListScaffold(onNavigate = { _, _ -> })
    }
}