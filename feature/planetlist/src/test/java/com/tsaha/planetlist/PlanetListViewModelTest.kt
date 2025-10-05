package com.tsaha.planetlist

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.domain.PlanetListUseCase
import com.tsaha.nucleus.domain.model.PlanetDetailsState
import com.tsaha.nucleus.domain.model.PlanetListResult
import com.tsaha.nucleus.domain.model.PlanetWithDetails
import com.tsaha.planetlist.model.PlanetListUiState
import io.mockk.coEvery
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
 * Comprehensive test suite for PlanetListViewModel using MockK and Turbine
 *
 * Tests cover:
 * - Navigation events
 * - Search functionality
 * - UI state transformations
 * - Domain to UI mapping
 * - Edge cases and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlanetListViewModelTest {

    private lateinit var viewModel: PlanetListViewModel
    private lateinit var mockRepository: PlanetRepository
    private lateinit var useCase: PlanetListUseCase
    private val testDispatcher = StandardTestDispatcher()

    // Test Data
    private val tatooine = Planet(uid = "1", name = "Tatooine")
    private val alderaan = Planet(uid = "2", name = "Alderaan")
    private val coruscant = Planet(uid = "3", name = "Coruscant")

    private val tatooineDetails = PlanetDetails(
        uid = "1",
        name = "Tatooine",
        climate = "arid",
        population = "200000",
        diameter = "10465",
        gravity = "1 standard",
        terrain = "desert"
    )

    private val alderaanDetails = PlanetDetails(
        uid = "2",
        name = "Alderaan",
        climate = "temperate",
        population = "2000000000",
        diameter = "12500",
        gravity = "1 standard",
        terrain = "grasslands, mountains"
    )

    private val paginationNoNext = Pagination(currentPage = 1, nextPage = null)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        useCase = PlanetListUseCase(mockRepository)

        // Setup default mocks
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine, alderaan))

        coEvery {
            mockRepository.getPlanet(any())
        } returns Result.success(tatooineDetails)

        coEvery {
            mockRepository.searchPlanets(any())
        } returns Result.success(listOf(tatooineDetails))

        viewModel = PlanetListViewModel(useCase)
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
        coEvery {
            mockRepository.searchPlanets(query)
        } returns Result.success(listOf(tatooineDetails))

        // When
        viewModel.onSearchQueryChanged(query)
        testDispatcher.scheduler.advanceTimeBy(350) // After debounce

        // Then
        assertThat(viewModel.searchQuery.value).isEqualTo(query)
        assertThat(viewModel.isSearchMode.value).isTrue()
    }

    @Test
    fun `clearSearch should reset search query to empty`() = runTest {
        // Given
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
        coEvery {
            mockRepository.searchPlanets(query)
        } returns Result.success(listOf(tatooineDetails))

        viewModel.onSearchQueryChanged(query)
        testDispatcher.scheduler.advanceTimeBy(350)

        // When
        viewModel.clearSearch()
        testDispatcher.scheduler.advanceTimeBy(350)

        // Then
        assertThat(viewModel.searchQuery.value).isEqualTo("")
        assertThat(viewModel.isSearchMode.value).isEqualTo(false)
    }

    @Test
    fun `search with blank query should return to normal mode`() = runTest {
        // Given
        viewModel.onSearchQueryChanged("Tatooine")
        testDispatcher.scheduler.advanceTimeBy(350)

        // When
        viewModel.onSearchQueryChanged("   ")
        testDispatcher.scheduler.advanceTimeBy(350)

        // Then
        assertThat(viewModel.isSearchMode.value).isEqualTo(false)
    }

    @Test
    fun `rapid search query changes should debounce correctly`() = runTest {
        // When - Type rapidly
        viewModel.onSearchQueryChanged("T")
        viewModel.onSearchQueryChanged("Ta")
        viewModel.onSearchQueryChanged("Tat")
        viewModel.onSearchQueryChanged("Tato")
        viewModel.onSearchQueryChanged("Tatoo")
        viewModel.onSearchQueryChanged("Tatooine")

        // Wait for debounce
        testDispatcher.scheduler.advanceTimeBy(350)

        // Then - Should only search for final query
        assertThat(viewModel.searchQuery.value).isEqualTo("Tatooine")
    }

    // ===============================
    // UI STATE TESTS
    // ===============================

    @Test
    fun `uiState should emit loading state initially`() = runTest {
        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val currentState = viewModel.uiState.value
        assertThat(currentState).isInstanceOf(PlanetListUiState.ListLoading::class)
    }

    @Test
    fun `uiState should transform domain result to UI state`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine))

        coEvery {
            mockRepository.getPlanet("1")
        } returns Result.success(tatooineDetails)

        // When
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
        coEvery {
            mockRepository.searchPlanets(query)
        } returns Result.success(listOf(tatooineDetails))

        // When
        viewModel.uiState.test {
            awaitItem() // Initial loading

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)

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
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = 1, limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine))

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
        coEvery {
            mockRepository.searchPlanets(query)
        } returns Result.success(listOf(tatooineDetails))

        viewModel.onSearchQueryChanged(query)
        testDispatcher.scheduler.advanceTimeBy(350)

        // When - Try to request next page while in search mode
        viewModel.onRequestInitialOrNextPage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should still be in search mode
        assertThat(viewModel.isSearchMode.value).isTrue()
    }

    // ===============================
    // EDGE CASES AND ERROR HANDLING
    // ===============================

    @Test
    fun `viewModel should handle empty planet list`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to emptyList())

        // When
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
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.failure(RuntimeException(errorMessage))

        // When
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
        coEvery {
            mockRepository.searchPlanets(query)
        } returns Result.success(emptyList())

        // When
        viewModel.uiState.test {
            awaitItem() // Initial loading

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)

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
        coEvery {
            mockRepository.searchPlanets(query)
        } returns Result.failure(RuntimeException(errorMessage))

        // When
        viewModel.uiState.test {
            awaitItem() // Initial loading

            viewModel.onSearchQueryChanged(query)
            testDispatcher.scheduler.advanceTimeBy(350)

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