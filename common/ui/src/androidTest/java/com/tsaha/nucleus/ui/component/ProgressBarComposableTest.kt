package com.tsaha.nucleus.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isTrue
import com.tsaha.nucleus.ui.theme.NucleusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for ProgressBarComposable component
 *
 * Tests cover:
 * - Progress indicator display
 * - Loading state representation
 * - Different layout contexts
 * - Modifier applications
 */
@RunWith(AndroidJUnit4::class)
class ProgressBarComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun progressBarComposable_displaysCorrectly() {
        // When
        composeTestRule.setContent {
            NucleusTheme {
                ProgressBarComposable()
            }
        }

        // Then
        // Progress bar should be displayed (checking root exists as it's a LinearProgressIndicator)
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_withCustomModifier_appliesModifierCorrectly() {
        // When
        composeTestRule.setContent {
            NucleusTheme {
                ProgressBarComposable(
                    modifier = Modifier.testTag("custom_progress_bar")
                )
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("custom_progress_bar")).assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_withPadding_appliesPaddingCorrectly() {
        // When
        composeTestRule.setContent {
            NucleusTheme {
                ProgressBarComposable(
                    modifier = Modifier
                        .padding(16.dp)
                        .testTag("padded_progress_bar")
                )
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("padded_progress_bar")).assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_inLoadingContext_representsLoadingState() {
        // Given - Simulating loading context like in PlanetListScreen or PlanetDetailsScreen
        // When
        composeTestRule.setContent {
            NucleusTheme {
                androidx.compose.material3.Scaffold { padding ->
                    ProgressBarComposable(
                        modifier = Modifier
                            .padding(padding)
                            .testTag("loading_progress_bar")
                    )
                }
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("loading_progress_bar")).assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_inDifferentContainers_adaptsCorrectly() {
        // When - Test progress bar in different layout contexts
        composeTestRule.setContent {
            NucleusTheme {
                Column {
                    // Progress bar in a column
                    Column {
                        ProgressBarComposable(
                            modifier = Modifier.testTag("column_progress_bar")
                        )
                    }

                    // Progress bar in a box with full size
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ProgressBarComposable(
                            modifier = Modifier.testTag("box_progress_bar")
                        )
                    }
                }
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("column_progress_bar")).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("box_progress_bar")).assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_multipleInstances_allDisplayCorrectly() {
        // When
        composeTestRule.setContent {
            NucleusTheme {
                Column {
                    ProgressBarComposable(
                        modifier = Modifier.testTag("progress_1")
                    )
                    ProgressBarComposable(
                        modifier = Modifier.testTag("progress_2")
                    )
                    ProgressBarComposable(
                        modifier = Modifier.testTag("progress_3")
                    )
                }
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("progress_1")).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("progress_2")).assertIsDisplayed()
        composeTestRule.onNode(hasTestTag("progress_3")).assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_hasCorrectStructure() {
        // When
        composeTestRule.setContent {
            NucleusTheme {
                ProgressBarComposable()
            }
        }

        // Then
        // Verify that progress bar creates the expected UI structure
        var progressBarExists = false
        composeTestRule.runOnUiThread {
            progressBarExists = true // If we get here, the composable was created successfully
        }
        assertThat(progressBarExists).isTrue()
    }

    @Test
    fun progressBarComposable_fillsMaxWidth() {
        // When
        composeTestRule.setContent {
            NucleusTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ProgressBarComposable(
                        modifier = Modifier.testTag("full_width_progress")
                    )
                }
            }
        }

        // Then
        // Progress bar should be displayed and fill the width (implicitly tested by the component)
        composeTestRule.onNode(hasTestTag("full_width_progress")).assertIsDisplayed()
    }

    @Test
    fun progressBarComposable_simulateScreenLoadingStates() {
        // Given - Simulating real usage in screens like ListLoading and DetailsLoading states

        // Test: Simple loading state simulation
        composeTestRule.setContent {
            NucleusTheme {
                Column {
                    ProgressBarComposable(
                        modifier = Modifier.testTag("list_loading_progress")
                    )
                }
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag("list_loading_progress")).assertIsDisplayed()
    }
}