package com.tsaha.planetlist

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.planetlist.model.PlanetListUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanetListViewModelTest {

    private lateinit var viewModel: PlanetListViewModel
    private lateinit var mockRepository: MockPlanetRepository
    private lateinit var useCase: PlanetListUseCase
    private val testDispatcher = StandardTestDispatcher()

    // Test Data
    private val tatooine = Planet(uid = "1", name = "Tatooine")
    private val alderaan = Planet(uid = "2", name = "Alderaan")
    private val coruscant = Planet(uid = "3", name = "Coruscant")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockPlanetRepository()
        useCase = PlanetListUseCase(mockRepository)
        viewModel = PlanetListViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===============================
    // NAVIGATION EVENT TESTS - These test the SharedFlow events
    // ===============================

    @Test
    fun `onClickPlanet should emit navigation event with correct UID`() = runTest {
        // Given
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

        // When & Then
        viewModel.navEvent.test {
            viewModel.onClickPlanet(planetWithEmptyId)

            val event = awaitItem()
            assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo("")

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onClickPlanet should handle stress test with many rapid clicks`() = runTest {
        // When & Then
        viewModel.navEvent.test {
            // Simulate rapid clicks (stress test)
            repeat(10) { index ->
                val planet = Planet(uid = index.toString(), name = "Planet $index")
                viewModel.onClickPlanet(planet)

                val event = awaitItem()
                assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo(index.toString())
            }

            ensureAllEventsConsumed()
        }
    }

    // ===============================
    // UI STATE TESTS
    // ===============================

    @Test
    fun `uiState should have initial loading value`() = runTest {
        // When
        val initialState = viewModel.uiState.value

        // Then
        assertThat(initialState is PlanetListUiState.ListLoading).isTrue()
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
        private val mockPlanets = listOf(
            Planet(uid = "1", name = "Tatooine"),
            Planet(uid = "2", name = "Alderaan")
        )

        private val mockPlanetDetails = PlanetDetails(
            uid = "1", name = "Tatooine", climate = "arid",
            population = "200000", diameter = "10465",
            gravity = "1 standard", terrain = "desert"
        )

        override suspend fun getPlanetsWithPagination(
            pageNumber: Int,
            limit: Int
        ): Result<Pair<Pagination, List<Planet>>> {
            return Result.success(Pagination(1, null) to mockPlanets)
        }

        override suspend fun getPlanetsWithPagination(limit: Int): Result<Pair<Pagination, List<Planet>>> {
            return getPlanetsWithPagination(1, limit)
        }

        override suspend fun getPlanet(id: String): Result<PlanetDetails> {
            return Result.success(mockPlanetDetails)
        }
    }
}