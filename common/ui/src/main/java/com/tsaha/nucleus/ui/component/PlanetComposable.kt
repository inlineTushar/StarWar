package com.tsaha.nucleus.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun PlanetComposable(
    headlineContent: @Composable () -> Unit,
    subHeadingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = headlineContent,
        supportingContent = subHeadingContent,
        modifier =
            if (onClick != null)
                modifier.debouncedClickable(onClickLabel = label) { onClick() }
            else modifier
    )
}

@Composable
fun PlanetNameComposable(
    name: String,
    modifier: Modifier = Modifier,
    stylable: Boolean = false
) {
    Text(
        text = name,
        modifier = modifier.fillMaxWidth(),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        style = if (stylable) {
            MaterialTheme.typography.headlineLarge
                .copy(
                    fontSize = 48.sp,
                    brush = Brush.linearGradient(
                        colors = remember {
                            listOf(
                                Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)),
                                Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                            )
                        },
                        tileMode = TileMode.Repeated
                    )
                )
        }
        else MaterialTheme.typography.headlineMedium
    )
}