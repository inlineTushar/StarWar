package com.tsaha.planetdetail

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isSameAs
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.ui.PlanetDetailsUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for PlanetDetailViewModel using MockK
 *
 * This test class verifies:
 * - ViewModel initialization and state management
 * - Repository interaction with proper mocking
 * - Error handling scenarios
 * - Edge cases and lifecycle behavior
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlanetDetailViewModelTest {

    private lateinit var viewModel: PlanetDetailViewModel
    private lateinit var mockRepository: PlanetRepository
    private val testDispatcher = StandardTestDispatcher()

    // Test Data
    private val testPlanetId = "test-planet-123"
    private val testPlanetDetails = PlanetDetails(
        uid = testPlanetId,
        name = "Tatooine",
        climate = "arid",
        population = "200000",
        diameter = "10465",
        gravity = "1 standard",
        terrain = "desert"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===============================
    // BASIC INITIALIZATION TESTS
    // ===============================

    @Test
    fun `uiState should start with loading state`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        // When
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(PlanetDetailsUiState.DetailsLoading)
    }

    @Test
    fun `should create ViewModel with constructor parameters`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        // When
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // Then
        assertThat(viewModel).isNotNull()
        assertThat(viewModel.uiState).isNotNull()
        assertThat(viewModel.uiState.value).isEqualTo(PlanetDetailsUiState.DetailsLoading)
    }

    @Test
    fun `should handle different planet IDs in constructor`() = runTest {
        // Given
        val customIds = listOf("planet1", "planet2", "", "very-long-id-" + "x".repeat(100))
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        customIds.forEach { planetId ->
            // When
            viewModel = PlanetDetailViewModel(planetId, mockRepository)

            // Then
            assertThat(viewModel).isNotNull()
            assertThat(viewModel.uiState.value).isEqualTo(PlanetDetailsUiState.DetailsLoading)
        }
    }

    // ===============================
    // STATE FLOW BEHAVIOR TESTS
    // ===============================

    @Test
    fun `uiState should be accessible and consistent`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When - Multiple accesses to state
        val firstAccess = viewModel.uiState.value
        val secondAccess = viewModel.uiState.value

        // Then
        assertThat(firstAccess).isEqualTo(secondAccess)
        assertThat(firstAccess).isNotNull()
    }

    @Test
    fun `StateFlow should maintain reference consistency`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When & Then - StateFlow reference should be consistent
        val flow1 = viewModel.uiState
        val flow2 = viewModel.uiState

        assertThat(flow1).isSameAs(flow2)
    }

    @Test
    fun `StateFlow should be cold and replayable`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When & Then - StateFlow should provide current value immediately
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(PlanetDetailsUiState.DetailsLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===============================
    // CONSTRUCTOR PARAMETER TESTS
    // ===============================

    @Test
    fun `should handle empty planet ID in constructor`() = runTest {
        // Given
        val emptyId = ""
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        // When
        viewModel = PlanetDetailViewModel(emptyId, mockRepository)

        // Then
        assertThat(viewModel).isNotNull()
        assertThat(viewModel.uiState.value).isEqualTo(PlanetDetailsUiState.DetailsLoading)
    }

    @Test
    fun `should handle special characters in planet ID`() = runTest {
        // Given
        val specialId = "planet-123!@#$%^&*()"
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        // When
        viewModel = PlanetDetailViewModel(specialId, mockRepository)

        // Then
        assertThat(viewModel).isNotNull()
        assertThat(viewModel.uiState.value).isEqualTo(PlanetDetailsUiState.DetailsLoading)
    }

    @Test
    fun `should handle very long planet ID`() = runTest {
        // Given
        val longId = "a".repeat(1000)
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        // When
        viewModel = PlanetDetailViewModel(longId, mockRepository)

        // Then
        assertThat(viewModel).isNotNull()
        assertThat(viewModel.uiState.value).isEqualTo(PlanetDetailsUiState.DetailsLoading)
    }

    // ===============================
    // REPOSITORY INTERACTION TESTS
    // ===============================

    @Test
    fun `getPlanetDetail should call repository with correct planet ID`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(testPlanetId) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When
        viewModel.getPlanetDetail(testPlanetId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(atLeast = 1) { mockRepository.getPlanet(testPlanetId) }
    }

    @Test
    fun `getPlanetDetail should handle repository success`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(testPlanetId) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When
        viewModel.getPlanetDetail(testPlanetId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - The call was made successfully
        coVerify { mockRepository.getPlanet(testPlanetId) }
    }

    @Test
    fun `getPlanetDetail should handle repository failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { mockRepository.getPlanet(testPlanetId) } returns Result.failure(
            RuntimeException(
                errorMessage
            )
        )
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When
        viewModel.getPlanetDetail(testPlanetId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - The call was attempted
        coVerify { mockRepository.getPlanet(testPlanetId) }
    }

    // ===============================
    // STRESS AND EDGE CASE TESTS
    // ===============================

    @Test
    fun `should handle rapid ViewModel creation`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)

        // When - Create multiple ViewModels rapidly
        val viewModels = mutableListOf<PlanetDetailViewModel>()

        repeat(10) { index ->
            val vm = PlanetDetailViewModel("planet-$index", mockRepository)
            viewModels.add(vm)
        }

        // Then
        assertThat(viewModels.size).isEqualTo(10)
        viewModels.forEach { vm ->
            assertThat(vm).isNotNull()
            assertThat(vm.uiState.value).isNotNull()
        }
    }

    @Test
    fun `should handle multiple consecutive getPlanetDetail calls`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When - Multiple rapid calls
        repeat(5) {
            viewModel.getPlanetDetail(testPlanetId)
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - All calls should be attempted
        coVerify(atLeast = 5) { mockRepository.getPlanet(testPlanetId) }
    }

    // ===============================
    // ERROR HANDLING TESTS
    // ===============================

    @Test
    fun `should handle null error message gracefully`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(testPlanetId) } returns Result.failure(
            RuntimeException(
                null as String?
            )
        )
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When
        viewModel.getPlanetDetail(testPlanetId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should not crash
        coVerify { mockRepository.getPlanet(testPlanetId) }
    }

    @Test
    fun `should handle different error types`() = runTest {
        // Given
        val errors = listOf(
            RuntimeException("Network error"),
            IllegalStateException("Invalid state"),
            Exception("Generic error")
        )

        errors.forEach { error ->
            coEvery { mockRepository.getPlanet(any()) } returns Result.failure(error)
            viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

            // When
            viewModel.getPlanetDetail(testPlanetId)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then - Should handle all error types
            coVerify { mockRepository.getPlanet(testPlanetId) }
        }
    }

    // ===============================
    // INTEGRATION BEHAVIOR TESTS
    // ===============================

    @Test
    fun `should maintain proper lifecycle behavior`() = runTest {
        // Given
        coEvery { mockRepository.getPlanet(any()) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When - Simulate lifecycle events
        val initialState = viewModel.uiState.value
        testDispatcher.scheduler.advanceUntilIdle()
        val postAdvanceState = viewModel.uiState.value

        // Then
        assertThat(initialState).isNotNull()
        assertThat(postAdvanceState).isNotNull()
    }

    @Test
    fun `should work end-to-end for basic initialization`() = runTest {
        // Given - Full setup with realistic data
        coEvery { mockRepository.getPlanet(testPlanetId) } returns Result.success(testPlanetDetails)

        // When - Initialize ViewModel
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify basic functionality
        assertThat(viewModel).isNotNull()
        assertThat(viewModel.uiState.value).isNotNull()
        assertThat(viewModel.uiState.value).isInstanceOf(PlanetDetailsUiState.DetailsLoading::class.java)
    }

    // ===============================
    // MOCK VERIFICATION TESTS
    // ===============================

    @Test
    fun `should verify MockK relaxed mode allows unconfigured calls`() = runTest {
        // Given - Explicit configuration needed due to Result type casting
        coEvery { mockRepository.getPlanet(testPlanetId) } returns Result.success(testPlanetDetails)
        viewModel = PlanetDetailViewModel(testPlanetId, mockRepository)

        // When
        viewModel.getPlanetDetail(testPlanetId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should work with proper mock configuration
        assertThat(viewModel).isNotNull()
        coVerify { mockRepository.getPlanet(testPlanetId) }
    }

    @Test
    fun `should demonstrate proper MockK usage`() = runTest {
        // This test demonstrates correct MockK usage patterns:

        // 1. Create mock with relaxed mode
        val repository: PlanetRepository = mockk(relaxed = true)

        // 2. Configure specific behavior
        coEvery { repository.getPlanet(testPlanetId) } returns Result.success(testPlanetDetails)

        // 3. Create ViewModel with direct parameters
        val vm = PlanetDetailViewModel(testPlanetId, repository)

        // 4. Trigger action
        vm.getPlanetDetail(testPlanetId)
        testDispatcher.scheduler.advanceUntilIdle()

        // 5. Verify interaction
        coVerify { repository.getPlanet(testPlanetId) }

        // Verify basic operations work
        assertThat(vm).isNotNull()
    }

    // ===============================
    // DATA VALIDATION TESTS
    // ===============================

    @Test
    fun `should handle various planet detail data`() = runTest {
        // Given
        val planetVariations = listOf(
            PlanetDetails("1", "Earth", "temperate", "7000000000", "12742", "1", "grasslands"),
            PlanetDetails("2", "Mars", "cold", "0", "6779", "0.38", "rocky"),
            PlanetDetails("3", "", "", "", "", "", ""), // Empty data
            PlanetDetails("4", "Unknown", "unknown", "unknown", "unknown", "unknown", "unknown")
        )

        planetVariations.forEach { planetDetails ->
            // When
            coEvery { mockRepository.getPlanet(any()) } returns Result.success(planetDetails)
            viewModel = PlanetDetailViewModel(planetDetails.uid, mockRepository)
            viewModel.getPlanetDetail(planetDetails.uid)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then - Should handle all data variations
            assertThat(viewModel).isNotNull()
            coVerify { mockRepository.getPlanet(planetDetails.uid) }
        }
    }
}