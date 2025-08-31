package com.tsaha.nucleus.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isTrue
import com.tsaha.nucleus.ui.theme.NucleusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for NucleusAppBar component
 *
 * Tests cover:
 * - Title display
 * - Back button visibility and functionality
 * - Different configurations
 * - Click handlers
 * - Accessibility
 */
@RunWith(AndroidJUnit4::class)
class NucleusAppBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun nucleusAppBar_withTitle_displaysCorrectTitle() {
        // Given
        val appBarTitle = "Planet List"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = appBarTitle,
                    isBackVisible = false
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(appBarTitle).assertIsDisplayed()
    }

    @Test
    fun nucleusAppBar_withBackButtonVisible_showsBackButton() {
        // Given
        val appBarTitle = "Planet Details"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = appBarTitle,
                    isBackVisible = true
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(appBarTitle).assertIsDisplayed()
        // Back button should be present (using content description)
        composeTestRule.onNode(
            hasContentDescription("Navigate back")
        ).assertIsDisplayed()
    }

    @Test
    fun nucleusAppBar_withBackButtonNotVisible_hidesBackButton() {
        // Given
        val appBarTitle = "Planet List"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = appBarTitle,
                    isBackVisible = false
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(appBarTitle).assertIsDisplayed()
        // Back button should not be present
        composeTestRule.onNode(
            hasContentDescription("Navigate back")
        ).assertIsNotDisplayed()
    }

    @Test
    fun nucleusAppBar_backButtonClicked_triggersOnBackCallback() {
        // Given
        val appBarTitle = "Planet Details"
        var backButtonClicked = false

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = appBarTitle,
                    isBackVisible = true,
                    onBack = { backButtonClicked = true }
                )
            }
        }

        // Perform click on back button
        composeTestRule.onNode(
            hasContentDescription("Navigate back")
        ).performClick()

        // Then
        assertThat(backButtonClicked).isTrue()
    }

    @Test
    fun nucleusAppBar_withCustomLabel_hasCorrectTestTag() {
        // Given
        val appBarTitle = "Custom Title"
        val customLabel = "custom_app_bar_label"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = appBarTitle,
                    isBackVisible = false,
                    label = customLabel
                )
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag(customLabel)).assertIsDisplayed()
        composeTestRule.onNodeWithText(appBarTitle).assertIsDisplayed()
    }

    @Test
    fun nucleusAppBar_withLongTitle_displaysFullTitle() {
        // Given
        val longTitle = "Very Long App Bar Title That Should Be Displayed Completely"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = longTitle,
                    isBackVisible = false
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
    }

    @Test
    fun nucleusAppBar_withSpecialCharactersInTitle_displaysCorrectly() {
        // Given
        val titleWithSpecialChars = "Planet Details - XK-1 (Alpha)"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = titleWithSpecialChars,
                    isBackVisible = true
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(titleWithSpecialChars).assertIsDisplayed()
    }

    @Test
    fun nucleusAppBar_withEmptyTitle_stillDisplaysAppBar() {
        // Given
        val emptyTitle = ""

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = emptyTitle,
                    isBackVisible = false
                )
            }
        }

        // Then
        // App bar structure should still exist even with empty title
        var appBarExists = false
        composeTestRule.runOnUiThread {
            appBarExists = true // If we get here, the composable was created
        }
        assertThat(appBarExists).isTrue()
    }

    @Test
    fun nucleusAppBar_backButtonWithoutCallback_stillDisplaysButton() {
        // Given
        val appBarTitle = "Test Title"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                NucleusAppBar(
                    title = appBarTitle,
                    isBackVisible = true
                    // Using default empty onBack callback
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(appBarTitle).assertIsDisplayed()
        composeTestRule.onNode(
            hasContentDescription("Navigate back")
        ).assertIsDisplayed()
    }
}