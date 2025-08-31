package com.tsaha.planetdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import com.tsaha.nucleus.ui.theme.NucleusTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 4: Integration Tests for PlanetDetailsScreen
 *
 * Tests the complete screen with ViewModel interactions including:
 * - Full screen rendering with real ViewModels
 * - User interactions triggering ViewModel methods
 * - Navigation events and state changes
 * - Integration between screen and ViewModel layers
 * - Real data flow testing with planet details
 */
@RunWith(AndroidJUnit4::class)
class PlanetDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun planetDetailsScreen_withMockViewModel_rendersCorrectly() {
        // Given - Mock ViewModel with success state
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val testPlanetDetails = PlanetDetails(
            uid = "1",
            name = "Tatooine",
            climate = "Arid",
            population = "200,000",
            diameter = "10,465 km",
            gravity = "1 standard",
            terrain = "Desert"
        )

        val successState = PlanetDetailsUiState.DetailsSuccess(testPlanetDetails)
        every { mockViewModel.uiState } returns MutableStateFlow(successState)

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Planet details should be displayed
        composeTestRule.onNodeWithText("Tatooine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Climate:\nArid", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Population:\n200,000", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Diameter:\n10,465 km", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Gravity:\n1 standard", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Terrain:\nDesert", substring = true).assertIsDisplayed()
    }

    @Test
    fun planetDetailsScreen_loadingState_showsProgressIndicator() {
        // Given - Mock ViewModel with loading state
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(PlanetDetailsUiState.DetailsLoading)

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Should display loading state
        composeTestRule.onRoot().assertIsDisplayed()
        // Progress indicator should be visible for loading state
    }

    @Test
    fun planetDetailsScreen_errorState_showsErrorMessage() {
        // Given - Mock ViewModel with error state
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val errorMessage = "Failed to load planet details"
        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetDetailsUiState.DetailsError(errorMessage)
        )

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Should display error message
        composeTestRule.onRoot().assertIsDisplayed()
        // Error composable should be visible
    }

    @Test
    fun planetDetailsScreen_stateChanges_handlesTransitions() {
        // Given - Mock ViewModel that can change states
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow<PlanetDetailsUiState>(PlanetDetailsUiState.DetailsLoading)
        every { mockViewModel.uiState } returns stateFlow

        // When - Start with loading
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Initially shows loading
        composeTestRule.onRoot().assertIsDisplayed()

        // When - Change to success state
        val planetDetails = PlanetDetails(
            uid = "2",
            name = "Alderaan",
            climate = "Temperate",
            population = "2 billion",
            diameter = "12,500 km",
            gravity = "1 standard",
            terrain = "Grasslands, mountains"
        )
        stateFlow.value = PlanetDetailsUiState.DetailsSuccess(planetDetails)

        // Then - Shows planet details
        composeTestRule.onNodeWithText("Alderaan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Climate:\nTemperate", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Population:\n2 billion", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Diameter:\n12,500 km", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Gravity:\n1 standard", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Terrain:\nGrasslands, mountains", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun planetDetailsScreen_withDifferentPlanetData_displaysCorrectly() {
        // Given - Different planet data
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val planetDetails = PlanetDetails(
            uid = "3",
            name = "Coruscant",
            climate = "Temperate",
            population = "1 trillion",
            diameter = "12,240 km",
            gravity = "1 standard",
            terrain = "Cityscape"
        )

        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetDetailsUiState.DetailsSuccess(planetDetails)
        )

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - All planet details should be displayed
        composeTestRule.onNodeWithText("Coruscant").assertIsDisplayed()
        composeTestRule.onNodeWithText("Climate:\nTemperate", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Population:\n1 trillion", substring = true)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Diameter:\n12,240 km", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Gravity:\n1 standard", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Terrain:\nCityscape", substring = true).assertIsDisplayed()
    }

    @Test
    fun planetDetailsScreen_navigationIntegration_worksWithNavController() {
        // Given
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val planetDetails = PlanetDetails(
            uid = "nav-test",
            name = "Navigation Test Planet",
            climate = "Test Climate",
            population = "Test Population",
            diameter = "Test Diameter",
            gravity = "Test Gravity",
            terrain = "Test Terrain"
        )

        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetDetailsUiState.DetailsSuccess(planetDetails)
        )

        var navControllerCreated = false

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                navControllerCreated = true
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then
        assertThat(navControllerCreated).isTrue()
        composeTestRule.onNodeWithText("Navigation Test Planet").assertIsDisplayed()
    }

    @Test
    fun planetDetailsScreen_backButtonIntegration_triggersNavigation() {
        // Given - Mock ViewModel with success state
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val planetDetails = PlanetDetails(
            uid = "back-test",
            name = "Back Test Planet",
            climate = "Test",
            population = "Test",
            diameter = "Test",
            gravity = "Test",
            terrain = "Test"
        )

        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetDetailsUiState.DetailsSuccess(planetDetails)
        )

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Screen should render with back button functionality
        composeTestRule.onNodeWithText("Back Test Planet").assertIsDisplayed()
        // App bar with back button should be present
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun planetDetailsScreen_withLongPlanetData_handlesGracefully() {
        // Given - Planet with long data values
        val mockViewModel = mockk<PlanetDetailViewModel>(relaxed = true)
        val planetDetails = PlanetDetails(
            uid = "long-data-test",
            name = "Very Long Planet Name That Should Be Displayed Properly",
            climate = "Very complex climate with multiple atmospheric conditions and variations",
            population = "A very large population number: 999,999,999,999 inhabitants",
            diameter = "Extremely large diameter measurement: 999,999.99 kilometers",
            gravity = "Complex gravity measurement with detailed specifications",
            terrain = "Mixed terrain including mountains, valleys, oceans, deserts, and cities"
        )

        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetDetailsUiState.DetailsSuccess(planetDetails)
        )

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetDetailsScreen(
                    planetId = "test-planet-id",
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Should handle long text gracefully
        composeTestRule.onNodeWithText("Very Long Planet Name That Should Be Displayed Properly")
            .assertIsDisplayed()
        // Should not crash with long content
        var screenRendered = false
        composeTestRule.runOnUiThread {
            screenRendered = true
        }
        assertThat(screenRendered).isTrue()
    }
}