package com.tsaha.nucleus.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import com.tsaha.nucleus.ui.R

@Composable
fun ErrorComposable(
    errorText: String,
    modifier: Modifier = Modifier,
    tag: String = stringResource(R.string.common_ui_accessibility_error_occurred)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .semantics { testTag = tag }
    ) { Text(text = errorText, textAlign = TextAlign.Center) }
}