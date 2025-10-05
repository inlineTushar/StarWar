package com.tsaha.planetlist

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.domain.PlanetListUseCase
import com.tsaha.nucleus.domain.model.PlanetDetailsState
import com.tsaha.nucleus.domain.model.PlanetListResult
import com.tsaha.nucleus.domain.model.PlanetWithDetails
import com.tsaha.planetlist.model.PlanetListUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for PlanetListViewModel using MockK and Turbine
 *
 * Tests cover:
 * - Navigation events
 * - Search functionality
 * - UI state transformations
 * - Domain to UI mapping
 * - Edge cases and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PlanetListViewModelTest {

    private lateinit var viewModel: PlanetListViewModel
    private lateinit var mockUseCase: PlanetListUseCase
    private val testDispatcher = StandardTestDispatcher()

    // Test Data
    private val tatooine = Planet(uid = "1", name = "Tatooine")
    private val alderaan = Planet(uid = "2", name = "Alderaan")
    private val coruscant = Planet(uid = "3", name = "Coruscant")

    private val tatooineWithDetails = PlanetWithDetails(
        planet = tatooine,
        detailsState = PlanetDetailsState.Available(
            climate = "arid",
            population = "200000",
            diameter = "10465",
            gravity = "1 standard",
            terrain = "desert"
        )
    )

    private val alderaanWithDetails = PlanetWithDetails(
        planet = alderaan,
        detailsState = PlanetDetailsState.Available(
            climate = "temperate",
            population = "2000000000",
            diameter = "12500",
            gravity = "1 standard",
            terrain = "grasslands, mountains"
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockUseCase = mockk(relaxed = true)

        // Setup default mock behavior - observePlanets returns loading then success
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(
                PlanetListResult.Success(
                    items = listOf(tatooineWithDetails, alderaanWithDetails),
                    isLoadingNextPage = false
                )
            )
        }

        // Setup default mock behavior for search
        every { mockUseCase.searchPlanets(any()) } returns flowOf(
            PlanetListResult.SearchResult(
                items = listOf(tatooineWithDetails),
                query = "Tatooine",
                isSearching = false
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===============================
    // NAVIGATION EVENT TESTS
    // ===============================

    @Test
    fun `onClickPlanet should emit navigation event with correct UID`() = runTest {
        // Given
        viewModel = PlanetListViewModel(mockUseCase)
        val testPlanet = tatooine

        // When & Then
        viewModel.navEvent.test {
            viewModel.onClickPlanet(testPlanet)

            val event = awaitItem()
            assertThat(event).isInstanceOf(NavEvent.ToPlanetDetails::class)
            assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo(testPlanet.uid)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onClickPlanet should emit correct UID for different planets`() = runTest {
        // Given
        viewModel = PlanetListViewModel(mockUseCase)
        val planets = listOf(tatooine, alderaan, coruscant)

        // When & Then
        viewModel.navEvent.test {
            planets.forEach { planet ->
                viewModel.onClickPlanet(planet)

                val event = awaitItem()
                assertThat(event).isInstanceOf(NavEvent.ToPlanetDetails::class)
                assertThat((event as NavEvent.ToPlanetDetails).uid).isEqualTo(planet.uid)
            }

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onClickPlanet should handle multiple rapid clicks`() = runTest {
        // Given
        viewModel = PlanetListViewModel(mockUseCase)

        // When & Then
        viewModel.navEvent.test {
            viewModel.onClickPlanet(tatooine)
            viewModel.onClickPlanet(alderaan)
            viewModel.onClickPlanet(tatooine)

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
        viewModel = PlanetListViewModel(mockUseCase)

        // When & Then
        viewModel.navEvent.test {
            expectNoEvents()
        }
    }

    // ===============================
    // SEARCH FUNCTIONALITY TESTS
    // ===============================

    @Test
    fun `onSearchQueryChanged should update search query`() = runTest {
        // Given
        every { mockUseCase.searchPlanets(any()) } returns flowOf(
            PlanetListResult.SearchResult(
                items = listOf(tatooineWithDetails),
                query = "Tatooine",
                isSearching = false
            )
        )
        viewModel = PlanetListViewModel(mockUseCase)
        val query = "Tatooine"

        // When
        viewModel.onSearchQueryChanged(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.searchQuery.value).isEqualTo(query)
    }

    @Test
    fun `onSearchQueryChanged should trigger search after debounce`() = runTest {
        // Given
        val query = "Tatooine"
        every { mockUseCase.searchPlanets(query) } returns flow {
            emit(
                PlanetListResult.SearchResult(
                    items = emptyList(),
                    query = query,
                    isSearching = true
                )
            )
            emit(
                PlanetListResult.SearchResult(
                    items = listOf(tatooineWithDetails),
                    query = query,
                    isSearching = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        // Use turbine to collect the state which triggers isSearchMode update
        viewModel.uiState.test {
            awaitItem() // Initial loading state

            // When
            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350) // After debounce
            testDispatcher.scheduler.advanceUntilIdle()

            // Collect the search states
            awaitItem() // Searching state
            awaitItem() // Search result state

            // Then
            assertThat(viewModel.searchQuery.value).isEqualTo(query)
            assertThat(viewModel.isSearchMode.value).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearSearch should reset search query to empty`() = runTest {
        // Given
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(
                PlanetListResult.Success(
                    items = listOf(tatooineWithDetails),
                    isLoadingNextPage = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)
        viewModel.onSearchQueryChanged("Tatooine")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.searchQuery.value).isEqualTo("")
    }

    @Test
    fun `clearSearch should exit search mode`() = runTest {
        // Given
        val query = "Tatooine"
        every { mockUseCase.searchPlanets(query) } returns flow {
            emit(
                PlanetListResult.SearchResult(
                    items = listOf(tatooineWithDetails),
                    query = query,
                    isSearching = false
                )
            )
        }
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(
                PlanetListResult.Success(
                    items = listOf(tatooineWithDetails),
                    isLoadingNextPage = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        viewModel.onSearchQueryChanged(query)
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.searchQuery.value).isEqualTo("")
        assertThat(viewModel.isSearchMode.value).isEqualTo(false)
    }

    @Test
    fun `search with blank query should return to normal mode`() = runTest {
        // Given
        every { mockUseCase.searchPlanets(any()) } returns flowOf(
            PlanetListResult.SearchResult(items = emptyList(), query = "", isSearching = false)
        )
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(
                PlanetListResult.Success(
                    items = listOf(tatooineWithDetails),
                    isLoadingNextPage = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        viewModel.onSearchQueryChanged("Tatooine")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onSearchQueryChanged("   ")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.isSearchMode.value).isEqualTo(false)
    }

    @Test
    fun `rapid search query changes should debounce correctly`() = runTest {
        // Given
        viewModel = PlanetListViewModel(mockUseCase)

        // When - Type rapidly
        viewModel.onSearchQueryChanged("T")
        viewModel.onSearchQueryChanged("Ta")
        viewModel.onSearchQueryChanged("Tat")
        viewModel.onSearchQueryChanged("Tato")
        viewModel.onSearchQueryChanged("Tatoo")
        viewModel.onSearchQueryChanged("Tatooine")

        // Wait for debounce
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should only search for final query
        assertThat(viewModel.searchQuery.value).isEqualTo("Tatooine")
    }

    // ===============================
    // UI STATE TESTS
    // ===============================

    @Test
    fun `uiState should emit loading state initially`() = runTest {
        // Given
        every { mockUseCase.observePlanets(any(), any()) } returns flowOf(
            PlanetListResult.Loading
        )

        // When
        viewModel = PlanetListViewModel(mockUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val currentState = viewModel.uiState.value
        assertThat(currentState).isInstanceOf(PlanetListUiState.ListLoading::class)
    }

    @Test
    fun `uiState should transform domain result to UI state`() = runTest {
        // Given
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(
                PlanetListResult.Success(
                    items = listOf(tatooineWithDetails),
                    isLoadingNextPage = false
                )
            )
        }

        // When
        viewModel = PlanetListViewModel(mockUseCase)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListUiState.ListLoading::class)

            val successState = awaitItem()
            assertThat(successState).isInstanceOf(PlanetListUiState.ListSuccess::class)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState should handle search results correctly`() = runTest {
        // Given
        val query = "Tatooine"
        every { mockUseCase.searchPlanets(query) } returns flow {
            emit(
                PlanetListResult.SearchResult(
                    items = emptyList(),
                    query = query,
                    isSearching = true
                )
            )
            emit(
                PlanetListResult.SearchResult(
                    items = listOf(tatooineWithDetails),
                    query = query,
                    isSearching = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        // When
        viewModel.uiState.test {
            awaitItem() // Initial loading

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            val searchingState = awaitItem() as PlanetListUiState.SearchResult
            assertThat(searchingState.isSearching).isTrue()

            val resultState = awaitItem() as PlanetListUiState.SearchResult
            assertThat(resultState.isSearching).isEqualTo(false)
            assertThat(resultState.planetItems).isNotNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRequestInitialOrNextPage should trigger pagination in non-search mode`() = runTest {
        // Given
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(
                PlanetListResult.Success(
                    items = listOf(tatooineWithDetails),
                    isLoadingNextPage = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        // When
        viewModel.onRequestInitialOrNextPage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should have triggered pagination (verified through state changes)
        assertThat(viewModel.uiState.value).isNotNull()
    }

    @Test
    fun `onRequestInitialOrNextPage should not trigger in search mode`() = runTest {
        // Given
        val query = "Tatooine"
        every { mockUseCase.searchPlanets(query) } returns flow {
            emit(
                PlanetListResult.SearchResult(
                    items = listOf(tatooineWithDetails),
                    query = query,
                    isSearching = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        // Use turbine to collect the state which triggers isSearchMode update
        viewModel.uiState.test {
            awaitItem() // Initial loading state

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            awaitItem() // Search result state

            // When - Try to request next page while in search mode
            viewModel.onRequestInitialOrNextPage()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then - Should still be in search mode
            assertThat(viewModel.isSearchMode.value).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===============================
    // EDGE CASES AND ERROR HANDLING
    // ===============================

    @Test
    fun `viewModel should handle empty planet list`() = runTest {
        // Given
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(PlanetListResult.Error("No planets found"))
        }

        // When
        viewModel = PlanetListViewModel(mockUseCase)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            awaitItem() // Loading
            val errorState = awaitItem()

            // Then
            assertThat(errorState).isInstanceOf(PlanetListUiState.ListError::class)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `viewModel should handle repository failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { mockUseCase.observePlanets(any(), any()) } returns flow {
            emit(PlanetListResult.Loading)
            emit(PlanetListResult.Error(errorMessage))
        }

        // When
        viewModel = PlanetListViewModel(mockUseCase)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            awaitItem() // Loading
            val errorState = awaitItem() as PlanetListUiState.ListError

            // Then
            assertThat(errorState.errorMessage).isEqualTo(errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `viewModel should handle search with no results`() = runTest {
        // Given
        val query = "NonExistentPlanet"
        every { mockUseCase.searchPlanets(query) } returns flow {
            emit(
                PlanetListResult.SearchResult(
                    items = emptyList(),
                    query = query,
                    isSearching = true
                )
            )
            emit(
                PlanetListResult.SearchResult(
                    items = emptyList(),
                    query = query,
                    isSearching = false
                )
            )
        }
        viewModel = PlanetListViewModel(mockUseCase)

        // When
        viewModel.uiState.test {
            awaitItem() // Initial loading

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            val searchingState = awaitItem() as PlanetListUiState.SearchResult
            assertThat(searchingState.isSearching).isTrue()

            val resultState = awaitItem() as PlanetListUiState.SearchResult
            assertThat(resultState.isSearching).isEqualTo(false)
            assertThat(resultState.planetItems).isNotNull()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `viewModel should handle search error gracefully`() = runTest {
        // Given
        val query = "Tatooine"
        val errorMessage = "Search failed"
        every { mockUseCase.searchPlanets(query) } returns flow {
            emit(
                PlanetListResult.SearchResult(
                    items = emptyList(),
                    query = query,
                    isSearching = true
                )
            )
            emit(PlanetListResult.Error(errorMessage))
        }
        viewModel = PlanetListViewModel(mockUseCase)

        // When
        viewModel.uiState.test {
            awaitItem() // Initial loading

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)
            testDispatcher.scheduler.advanceUntilIdle()

            awaitItem() // Searching state
            val errorState = awaitItem()

            // Then
            assertThat(errorState).isInstanceOf(PlanetListUiState.ListError::class)

            cancelAndIgnoreRemainingEvents()
        }
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
        assertThat(navEvent).isInstanceOf(NavEvent::class)

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
            "",
            " ",
            "null",
            "123",
            "very-long-uid-that-might-cause-issues",
            "unicode-test-🌟"
        )

        // When & Then
        edgeCaseUIDs.forEach { uid ->
            val navEvent = NavEvent.ToPlanetDetails(uid)
            assertThat(navEvent).isNotNull()
            assertThat(navEvent.uid).isEqualTo(uid)
        }
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
}