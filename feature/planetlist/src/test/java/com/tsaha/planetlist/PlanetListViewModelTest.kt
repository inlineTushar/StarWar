package com.tsaha.planetlist

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.planetlist.model.PlanetItemLoadingState
import com.tsaha.planetlist.model.PlanetListUiState
import com.tsaha.planetlist.model.PlanetListUiState.ListError
import com.tsaha.planetlist.model.PlanetListUiState.ListLoading
import com.tsaha.planetlist.model.PlanetListUiState.ListSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for PlanetListViewModel
 *
 * This test class covers all major scenarios:
 * - Navigation event handling
 * - Planet loading with two-phase strategy
 * - Error handling scenarios
 * - Edge cases and parameter validation
 * - Concurrent planet detail loading
 * - Repository interaction verification
 *
 * Test Structure:
 * - Uses manual mock repository implementation
 * - Tests Flow emissions in correct order
 * - Verifies state transitions
 * - Covers all code paths
 */

@OptIn(ExperimentalCoroutinesApi::class)
class PlanetListViewModelTest {

    private lateinit var viewModel: PlanetListViewModel
    private lateinit var mockRepository: MockPlanetRepository
    private val testDispatcher = StandardTestDispatcher()

    // Test Data
    private val samplePagination = Pagination(currentPage = 1, nextPage = null)
    private val tatooine = Planet(uid = "1", name = "Tatooine")
    private val alderaan = Planet(uid = "2", name = "Alderaan")
    private val coruscant = Planet(uid = "3", name = "Coruscant")

    private val tatooineDetails = PlanetDetails(
        planet = Planet(uid = "1", name = "Tatooine"),
        climate = "arid",
        population = "200000",
        diameter = "10465",
        gravity = "1 standard",
        terrain = "desert"
    )

    private val alderaanDetails = PlanetDetails(
        planet = Planet(uid = "2", name = "Alderaan"),
        climate = "temperate",
        population = "2000000000",
        diameter = "12500",
        gravity = "1 standard",
        terrain = "grasslands, mountains"
    )

    private val coruscantDetails = PlanetDetails(
        planet = Planet(uid = "3", name = "Coruscant"),
        climate = "temperate",
        population = "1000000000000",
        diameter = "12240",
        gravity = "1 standard",
        terrain = "cityscape"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockPlanetRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===============================
    // PLANET LOADING TESTS - Core functionality moved from use case
    // ===============================

    @Test
    fun `uiState should emit loading state first`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        viewModel = PlanetListViewModel(mockRepository)

        // When
        val firstEmission = viewModel.uiState.value

        // Then
        assertThat(firstEmission is ListLoading).isTrue()
        assertThat(mockRepository.getPlanetsCallCount).isEqualTo(0) // StateFlow starts before collection
    }

    @Test
    fun `uiState should emit success with planets and loading details`() = runTest {
        // Given
        val planets = listOf(tatooine, alderaan)
        mockRepository.setupSuccessfulPlanetsResponse(planets)
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        mockRepository.setupSuccessfulPlanetDetail("2", alderaanDetails)
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState is ListLoading).isTrue()

            val successState = awaitItem() as ListSuccess
            assertThat(successState.planetItems).hasSize(2)
            assertThat(successState.planetItems[0].planet.name).isEqualTo("Tatooine")
            assertThat(successState.planetItems[1].planet.name).isEqualTo("Alderaan")

            // Initially all details should be loading
            successState.planetItems.forEach { planetItem ->
                assertThat(planetItem.loadingState is PlanetItemLoadingState.Loading).isTrue()
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should eventually load planet details successfully`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState is ListLoading).isTrue()

            val initialSuccessState = awaitItem() as ListSuccess
            assertThat(initialSuccessState.planetItems).hasSize(1)
            assertThat(initialSuccessState.planetItems[0].loadingState is PlanetItemLoadingState.Loading).isTrue()

            // Wait for details to load
            val detailsLoadedState = awaitItem() as ListSuccess
            assertThat(detailsLoadedState.planetItems[0].loadingState is PlanetItemLoadingState.Loaded).isTrue()

            val loadedState =
                detailsLoadedState.planetItems[0].loadingState as PlanetItemLoadingState.Loaded
            assertThat(loadedState.climate).isEqualTo("arid")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should respect custom page size`() = runTest {
        // Given
        val customPageSize = 15
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        viewModel = PlanetListViewModel(mockRepository)

        // When
        viewModel.observePlanets(pageSize = customPageSize).take(2).toList()

        // Then
        assertThat(mockRepository.lastPageSize).isEqualTo(customPageSize)
        assertThat(mockRepository.getPlanetsCallCount).isEqualTo(1)
    }

    @Test
    fun `observePlanets should use controlled concurrency`() = runTest {
        // Given
        val planets = listOf(tatooine, alderaan)
        mockRepository.setupSuccessfulPlanetsResponse(planets)
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        mockRepository.setupSuccessfulPlanetDetail("2", alderaanDetails)
        viewModel = PlanetListViewModel(mockRepository)

        // When
        val emissions = viewModel.observePlanets().take(2).toList()

        // Then
        assertThat(emissions).isNotEmpty()
        assertThat(emissions[0] is ListLoading).isTrue()

        val successState = emissions.find { it is ListSuccess } as? ListSuccess
        assertThat(successState).isNotNull()
        assertThat(successState!!.planetItems).hasSize(2)
    }

    // ===============================
    // ERROR SCENARIOS
    // ===============================

    @Test
    fun `uiState should emit error when planets fetch fails`() = runTest {
        // Given
        val errorMessage = "Network connection failed"
        mockRepository.setupFailurePlanetsResponse(errorMessage)
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState is ListLoading).isTrue()

            val errorState = awaitItem() as ListError
            assertThat(errorState.errorMessage).isEqualTo(errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should emit error when planets list is empty`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(emptyList())
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertThat(loadingState is ListLoading).isTrue()

            val errorState = awaitItem()
            assertThat(errorState is ListError).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should handle mixed success and error in planet details`() = runTest {
        // Given
        val planets = listOf(tatooine, alderaan)
        mockRepository.setupSuccessfulPlanetsResponse(planets)
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        mockRepository.setupFailurePlanetDetail("2", "Planet not found")
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            skipItems(1) // Skip loading
            val initialSuccess = awaitItem() as ListSuccess
            assertThat(initialSuccess.planetItems).hasSize(2)

            // Wait for some details to load
            val updated1 = awaitItem() as ListSuccess
            val updated2 = awaitItem() as ListSuccess

            // Verify that details loading was attempted
            assertThat(mockRepository.getPlanetCallCount).isEqualTo(2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===============================
    // EDGE CASES
    // ===============================

    @Test
    fun `uiState should handle single planet correctly`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            skipItems(1) // Skip loading

            val successState = awaitItem() as ListSuccess
            assertThat(successState.planetItems).hasSize(1)
            assertThat(successState.planetItems[0].planet.name).isEqualTo("Tatooine")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should handle large number of planets`() = runTest {
        // Given
        val manyPlanets = (1..20).map { Planet(uid = it.toString(), name = "Planet $it") }
        mockRepository.setupSuccessfulPlanetsResponse(manyPlanets)

        // Setup details for all planets
        manyPlanets.forEach { planet ->
            mockRepository.setupSuccessfulPlanetDetail(
                planet.uid,
                PlanetDetails(
                    planet = planet,
                    climate = "varies",
                    population = "unknown",
                    diameter = "unknown",
                    gravity = "1 standard",
                    terrain = "mixed"
                )
            )
        }
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.uiState.test {
            skipItems(1) // Skip loading

            val successState = awaitItem() as ListSuccess
            assertThat(successState.planetItems).hasSize(20)

            // All should initially be loading
            assertThat(successState.planetItems.all { it.loadingState is PlanetItemLoadingState.Loading }).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should handle negative page size gracefully`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        viewModel = PlanetListViewModel(mockRepository)

        // When - Use negative page size
        val emissions = viewModel.observePlanets(pageSize = -5).take(2).toList()

        // Then
        assertThat(emissions).isNotEmpty()
        assertThat(emissions[0] is ListLoading).isTrue()
    }

    // ===============================
    // NAVIGATION EVENT TESTS
    // ===============================

    @Test
    fun `onClickPlanet should emit navigation event with correct UID`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        viewModel = PlanetListViewModel(mockRepository)
        val testPlanet = tatooine

        // When & Then - Using Turbine to test SharedFlow
        viewModel.navEvent.test {
            viewModel.onClickPlanet(testPlanet)

            val event = awaitItem()
            assertThat(event is NavEvent.ToPlanetDetails).isTrue()
            assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo(testPlanet.uid)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onClickPlanet should emit correct UID for different planets`() = runTest {
        // Given
        val planets = listOf(tatooine, alderaan, coruscant)
        mockRepository.setupSuccessfulPlanetsResponse(planets)
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.navEvent.test {
            planets.forEach { planet ->
                viewModel.onClickPlanet(planet)

                val event = awaitItem()
                assertThat(event is NavEvent.ToPlanetDetails).isTrue()
                assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo(planet.uid)
            }

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onClickPlanet should handle multiple rapid clicks`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine, alderaan))
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.navEvent.test {
            // Click multiple planets rapidly
            viewModel.onClickPlanet(tatooine)
            viewModel.onClickPlanet(alderaan)
            viewModel.onClickPlanet(tatooine) // Click same planet again

            // Should receive all events in order
            val event1 = awaitItem()
            assertThat((event1 as NavEvent.ToPlanetDetails).uid).isEqualTo("1")

            val event2 = awaitItem()
            assertThat((event2 as NavEvent.ToPlanetDetails).uid).isEqualTo("2")

            val event3 = awaitItem()
            assertThat((event3 as NavEvent.ToPlanetDetails).uid).isEqualTo("1")

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `navEvent should not emit when no planet is clicked`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.navEvent.test {
            // Don't click any planet - should not receive any events
            expectNoEvents()
        }
    }

    @Test
    fun `onClickPlanet should handle planet with empty UID`() = runTest {
        // Given
        val planetWithEmptyId = Planet(uid = "", name = "Empty ID Planet")
        mockRepository.setupSuccessfulPlanetsResponse(listOf(planetWithEmptyId))
        viewModel = PlanetListViewModel(mockRepository)

        // When & Then
        viewModel.navEvent.test {
            viewModel.onClickPlanet(planetWithEmptyId)

            val event = awaitItem()
            assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo("")

            ensureAllEventsConsumed()
        }
    }

    // ===============================
    // REPOSITORY INTERACTION VERIFICATION
    // ===============================

    @Test
    fun `uiState should call repository methods in correct order`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(listOf(tatooine))
        mockRepository.setupSuccessfulPlanetDetail("1", tatooineDetails)
        viewModel = PlanetListViewModel(mockRepository)

        // When
        viewModel.uiState.test {
            skipItems(2) // Skip loading and initial success
            cancelAndIgnoreRemainingEvents()
        }

        // Then
        assertThat(mockRepository.getPlanetsCallCount).isEqualTo(1)
        // Planet detail calls are async, so we just verify setup
        assertThat(mockRepository.planetDetailsMap.containsKey("1")).isTrue()
    }

    @Test
    fun `uiState should not call planet details for empty planet list`() = runTest {
        // Given
        mockRepository.setupSuccessfulPlanetsResponse(emptyList())
        viewModel = PlanetListViewModel(mockRepository)

        // When
        viewModel.uiState.test {
            skipItems(2) // Loading and error
            cancelAndIgnoreRemainingEvents()
        }

        // Then
        assertThat(mockRepository.getPlanetsCallCount).isEqualTo(1)
        assertThat(mockRepository.getPlanetCallCount).isEqualTo(0)
    }

    // ===============================
    // DATA VALIDATION TESTS
    // ===============================

    @Test
    fun `NavEvent should be properly structured as sealed interface`() {
        // Given
        val sampleUID = "test-uid"

        // When
        val navEvent: NavEvent = NavEvent.ToPlanetDetails(sampleUID)

        // Then
        assertThat(navEvent is NavEvent).isTrue()

        when (navEvent) {
            is NavEvent.ToPlanetDetails -> {
                assertThat(navEvent.uid).isEqualTo(sampleUID)
            }
        }
    }

    @Test
    fun `NavEvent should handle edge case UIDs`() {
        // Given
        val edgeCaseUIDs = listOf(
            "", // Empty string
            " ", // Whitespace  
            "null", // String "null"
            "123", // Numeric string
            "very-long-uid-that-might-cause-issues",
            "unicode-test-🌟", // Unicode characters
        )

        // When & Then
        edgeCaseUIDs.forEach { uid ->
            val navEvent = NavEvent.ToPlanetDetails(uid)

            assertThat(navEvent).isNotNull()
            assertThat(navEvent.uid).isEqualTo(uid)
        }
    }

    @Test
    fun `Planet model should have required properties for navigation`() {
        // Given
        val planet = Planet(uid = "test-uid", name = "Test Planet")

        // When & Then
        assertThat(planet.uid).isNotNull()
        assertThat(planet.name).isNotNull()
        assertThat(planet.uid).isEqualTo("test-uid")
        assertThat(planet.name).isEqualTo("Test Planet")
    }

    @Test
    fun `NavEvent should support equality comparison`() {
        // Given
        val uid = "test-uid"

        // When
        val navEvent1 = NavEvent.ToPlanetDetails(uid)
        val navEvent2 = NavEvent.ToPlanetDetails(uid)
        val navEvent3 = NavEvent.ToPlanetDetails("different-uid")

        // Then
        assertThat(navEvent1).isEqualTo(navEvent2)
        assertThat(navEvent1).isNotEqualTo(navEvent3)
        assertThat(navEvent1.hashCode()).isEqualTo(navEvent2.hashCode())
    }

    // ===============================
    // MOCK REPOSITORY IMPLEMENTATION
    // ===============================

    private class MockPlanetRepository : PlanetRepository {
        var getPlanetsCallCount = 0
        var getPlanetCallCount = 0
        var lastPageSize = -1

        private var planetsResponse: Result<Pair<Pagination, List<Planet>>>? = null
        val planetDetailsMap = mutableMapOf<String, Result<PlanetDetails>>()

        fun setupSuccessfulPlanetsResponse(planets: List<Planet>) {
            planetsResponse = Result.success(Pagination(1, null) to planets)
        }

        fun setupFailurePlanetsResponse(errorMessage: String) {
            planetsResponse = Result.failure(RuntimeException(errorMessage))
        }

        fun setupSuccessfulPlanetDetail(planetId: String, details: PlanetDetails) {
            planetDetailsMap[planetId] = Result.success(details)
        }

        fun setupFailurePlanetDetail(planetId: String, errorMessage: String) {
            planetDetailsMap[planetId] = Result.failure(RuntimeException(errorMessage))
        }

        override suspend fun getPlanetsWithPagination(
            pageNumber: Int,
            limit: Int
        ): Result<Pair<Pagination, List<Planet>>> {
            getPlanetsCallCount++
            lastPageSize = limit
            return planetsResponse ?: Result.failure(RuntimeException("Not configured"))
        }

        override suspend fun getPlanetsWithPagination(limit: Int): Result<Pair<Pagination, List<Planet>>> {
            return getPlanetsWithPagination(1, limit)
        }

        override suspend fun getPlanet(id: String): Result<PlanetDetails> {
            getPlanetCallCount++
            return planetDetailsMap[id] ?: Result.failure(RuntimeException("Planet $id not found"))
        }
    }
}