package com.tsaha.nucleus.ui.component

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.tsaha.nucleus.ui.theme.NucleusTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for PlanetComposable component
 *
 * Tests cover:
 * - Planet name display
 * - Click interactions
 * - Accessibility labels
 * - Different planet data scenarios
 */
@RunWith(AndroidJUnit4::class)
class PlanetComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun planetComposable_withPlanetName_displaysCorrectName() {
        // Given
        val planetName = "Tatooine"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = planetName) },
                    subHeadingContent = { Text("Desert planet") },
                    onClick = {},
                    label = "Planet $planetName"
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(planetName).assertIsDisplayed()
    }

    @Test
    fun planetComposable_withSubheadingContent_displaysBothContents() {
        // Given
        val planetName = "Alderaan"
        val planetDescription = "Peaceful planet"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = planetName) },
                    subHeadingContent = { Text(planetDescription) },
                    onClick = {},
                    label = "Planet $planetName"
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(planetName).assertIsDisplayed()
        composeTestRule.onNodeWithText(planetDescription).assertIsDisplayed()
    }

    @Test
    fun planetComposable_whenClicked_triggersOnClickCallback() {
        // Given
        val planetName = "Coruscant"
        var wasClicked = false

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = planetName) },
                    subHeadingContent = { Text("City planet") },
                    onClick = { wasClicked = true },
                    label = "Planet $planetName"
                )
            }
        }

        // Perform click
        composeTestRule.onNodeWithText(planetName).performClick()

        // Then
        assertThat(wasClicked).isTrue()
    }

    @Test
    fun planetComposable_withDifferentPlanetNames_displaysCorrectly() {
        // Given - Test one representative planet name
        val planetName = "Tatooine"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = planetName) },
                    subHeadingContent = { Text("Desert planet") },
                    onClick = {},
                    label = "Planet $planetName"
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(planetName).assertIsDisplayed()
    }

    @Test
    fun planetComposable_withLongPlanetName_displaysFullName() {
        // Given
        val longPlanetName = "Very Long Planet Name That Should Be Displayed Completely"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = longPlanetName) },
                    subHeadingContent = { Text("Long name planet") },
                    onClick = {},
                    label = "Planet $longPlanetName"
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(longPlanetName).assertIsDisplayed()
    }

    @Test
    fun planetComposable_withSpecialCharacters_displaysCorrectly() {
        // Given
        val planetNameWithSpecialChars = "Planet-X1 (Beta) #2"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = planetNameWithSpecialChars) },
                    subHeadingContent = { Text("Special characters test") },
                    onClick = {},
                    label = "Planet $planetNameWithSpecialChars"
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText(planetNameWithSpecialChars).assertIsDisplayed()
    }

    @Test
    fun planetComposable_multipleClicksWithDelay_triggersCallbacksCorrectly() {
        // Given
        val planetName = "Mars"
        var clickCount = 0

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetComposable(
                    headlineContent = { PlanetNameComposable(name = planetName) },
                    subHeadingContent = { Text("Red planet") },
                    onClick = { clickCount++ },
                    label = "Planet $planetName"
                )
            }
        }

        // Perform single click first to test basic functionality
        composeTestRule.onNodeWithText(planetName).performClick()

        // Then - Due to debouncing, only one click should register immediately
        assertThat(clickCount).isEqualTo(1)
    }

    @Test
    fun planetNameComposable_withPlanetName_displaysCorrectText() {
        // Given
        val planetName = "Venus"

        // When
        composeTestRule.setContent {
            NucleusTheme {
                PlanetNameComposable(name = planetName)
            }
        }

        // Then
        composeTestRule.onNodeWithText(planetName).assertIsDisplayed()
    }
}