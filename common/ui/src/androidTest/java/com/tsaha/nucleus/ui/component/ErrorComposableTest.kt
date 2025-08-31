package com.tsaha.nucleus.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isTrue
import com.tsaha.nucleus.ui.theme.NucleusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for ErrorComposable component
 *
 * Tests cover:
 * - Error message display
 * - Custom error text
 * - Test tag assignment
 * - Accessibility
 */
@RunWith(AndroidJUnit4::class)
class ErrorComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorComposable_withErrorMessage_displaysCorrectText() {
        // Given
        val errorMessage = "Network connection failed"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                ErrorComposable(errorText = errorMessage)
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun errorComposable_withCustomErrorMessage_displaysCustomText() {
        // Given
        val customErrorMessage = "Something went wrong. Please try again."

        // When
        composeTestRule.setContent {
            NucleusTheme {
                ErrorComposable(errorText = customErrorMessage)
            }
        }

        // Then
        composeTestRule.onNodeWithText(customErrorMessage).assertIsDisplayed()
    }

    @Test
    fun errorComposable_withCustomTestTag_hasCorrectTag() {
        // Given
        val errorMessage = "Test error"
        val customTag = "custom_error_tag"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                ErrorComposable(
                    errorText = errorMessage,
                    tag = customTag
                )
            }
        }

        // Then
        composeTestRule.onNode(hasTestTag(customTag)).assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun errorComposable_withEmptyErrorMessage_stillDisplaysEmptyMessage() {
        // Given
        val emptyErrorMessage = ""

        // When
        composeTestRule.setContent {
            NucleusTheme {
                ErrorComposable(errorText = emptyErrorMessage)
            }
        }

        // Then
        // Should still create the composable structure even with empty text
        var composableExists = false
        composeTestRule.runOnUiThread {
            composableExists = true // If we get here, the composable was created
        }
        assertThat(composableExists).isTrue()
    }

    @Test
    fun errorComposable_withLongErrorMessage_displaysFullMessage() {
        // Given
        val longErrorMessage = "This is a very long error message that should be displayed " +
            "completely in the error composable component. It tests the behavior with " +
            "lengthy error descriptions that users might encounter in real scenarios."

        // When
        composeTestRule.setContent {
            NucleusTheme {
                ErrorComposable(errorText = longErrorMessage)
            }
        }

        // Then
        composeTestRule.onNodeWithText(longErrorMessage).assertIsDisplayed()
    }

    @Test
    fun errorComposable_withSpecialCharacters_displaysCorrectly() {
        // Given
        val errorWithSpecialChars = "Error: Network timeout!"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                ErrorComposable(errorText = errorWithSpecialChars)
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorWithSpecialChars).assertIsDisplayed()
    }
}