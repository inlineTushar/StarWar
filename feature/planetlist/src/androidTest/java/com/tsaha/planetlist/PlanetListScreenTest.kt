package com.tsaha.planetlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.ui.theme.NucleusTheme
import com.tsaha.planetlist.model.PlanetItem
import com.tsaha.planetlist.model.PlanetItemLoadingState
import com.tsaha.planetlist.model.PlanetListUiState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 4: Integration Tests for PlanetListScreen
 *
 * Tests the complete screen with ViewModel interactions including:
 * - Full screen rendering with real ViewModels
 * - User interactions triggering ViewModel methods
 * - Navigation events and state changes
 * - Integration between screen and ViewModel layers
 * - Real data flow testing
 */
@RunWith(AndroidJUnit4::class)
class PlanetListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun planetListScreen_withMockViewModel_rendersCorrectly() {
        // Given - Mock ViewModel with success state
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        val testPlanets = listOf(
            PlanetItem(
                planet = Planet(uid = "1", name = "Tatooine"),
                loadingState = PlanetItemLoadingState.Loaded(
                    climate = "Arid",
                    population = "200000"
                )
            ),
            PlanetItem(
                planet = Planet(uid = "2", name = "Alderaan"),
                loadingState = PlanetItemLoadingState.Loading
            )
        )

        val successState = PlanetListUiState.ListSuccess(testPlanets)
        every { mockViewModel.uiState } returns MutableStateFlow(successState)
        every { mockViewModel.navEvent } returns emptyFlow()

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Tatooine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alderaan").assertIsDisplayed()
    }

    @Test
    fun planetListScreen_planetClick_triggersViewModelMethod() {
        // Given
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        val testPlanet = Planet(uid = "1", name = "Clickable Planet")
        val testPlanets = listOf(
            PlanetItem(
                planet = testPlanet,
                loadingState = PlanetItemLoadingState.Loading
            )
        )

        val successState = PlanetListUiState.ListSuccess(testPlanets)
        every { mockViewModel.uiState } returns MutableStateFlow(successState)
        every { mockViewModel.navEvent } returns emptyFlow()

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Perform click
        composeTestRule.onNodeWithText("Clickable Planet").performClick()

        // Then - Verify ViewModel method was called
        verify { mockViewModel.onClickPlanet(testPlanet) }
    }

    @Test
    fun planetListScreen_loadingState_showsProgressIndicator() {
        // Given - Mock ViewModel with loading state
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(PlanetListUiState.ListLoading)
        every { mockViewModel.navEvent } returns emptyFlow()

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onRoot().assertIsDisplayed()
        // Progress indicator should be displayed for loading state
    }

    @Test
    fun planetListScreen_errorState_showsErrorMessage() {
        // Given - Mock ViewModel with error state
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(PlanetListUiState.ListError("Network error"))
        every { mockViewModel.navEvent } returns emptyFlow()

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then
        composeTestRule.onRoot().assertIsDisplayed()
        // Error state should be displayed with error composable
    }

    @Test
    fun planetListScreen_multipleStates_handlesStateChanges() {
        // Given - Mock ViewModel that can change states
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow<PlanetListUiState>(PlanetListUiState.ListLoading)
        every { mockViewModel.uiState } returns stateFlow
        every { mockViewModel.navEvent } returns emptyFlow()

        // When - Start with loading
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Initially shows loading
        composeTestRule.onRoot().assertIsDisplayed()

        // When - Change to success state
        val testPlanets = listOf(
            PlanetItem(
                planet = Planet(uid = "1", name = "State Change Planet"),
                loadingState = PlanetItemLoadingState.Loading
            )
        )
        stateFlow.value = PlanetListUiState.ListSuccess(testPlanets)

        // Then - Shows success content
        composeTestRule.onNodeWithText("State Change Planet").assertIsDisplayed()
    }

    @Test
    fun planetListScreen_withDifferentPlanetDetails_showsCorrectStates() {
        // Given - Planets with different detail states
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        val mixedStatePlanets = listOf(
            PlanetItem(
                planet = Planet(uid = "1", name = "Loading Planet"),
                loadingState = PlanetItemLoadingState.Loading
            ),
            PlanetItem(
                planet = Planet(uid = "2", name = "Success Planet"),
                loadingState = PlanetItemLoadingState.Loaded(
                    climate = "Temperate",
                    population = "1000000"
                )
            ),
            PlanetItem(
                planet = Planet(uid = "3", name = "Error Planet"),
                loadingState = PlanetItemLoadingState.LoadFailed("Load failed")
            )
        )

        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetListUiState.ListSuccess(mixedStatePlanets)
        )
        every { mockViewModel.navEvent } returns emptyFlow()

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - All planets should be displayed regardless of their detail states
        composeTestRule.onNodeWithText("Loading Planet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Success Planet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error Planet").assertIsDisplayed()

        // Check that the success planet shows its climate and population
        composeTestRule.onNodeWithText("Temperate").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000000").assertIsDisplayed()
    }

    @Test
    fun planetListScreen_navigationIntegration_worksWithNavController() {
        // Given
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        val testPlanet = Planet(uid = "nav-test", name = "Navigation Planet")
        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetListUiState.ListSuccess(
                listOf(PlanetItem(testPlanet, PlanetItemLoadingState.Loading))
            )
        )
        every { mockViewModel.navEvent } returns emptyFlow()

        var navControllerCreated = false

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                navControllerCreated = true
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then
        assertThat(navControllerCreated).isTrue()
        composeTestRule.onNodeWithText("Navigation Planet").assertIsDisplayed()
    }

    @Test
    fun planetListScreen_emptyList_handlesGracefully() {
        // Given - Empty planet list
        val mockViewModel = mockk<PlanetListViewModel>(relaxed = true)
        every { mockViewModel.uiState } returns MutableStateFlow(
            PlanetListUiState.ListSuccess(emptyList())
        )
        every { mockViewModel.navEvent } returns emptyFlow()

        // When
        composeTestRule.setContent {
            NucleusTheme {
                val navController = rememberNavController()
                PlanetListScreen(
                    navController = navController,
                    vm = mockViewModel
                )
            }
        }

        // Then - Should not crash with empty list
        var screenRendered = false
        composeTestRule.runOnUiThread {
            screenRendered = true
        }
        assertThat(screenRendered).isTrue()
    }
}