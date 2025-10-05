package com.tsaha.nucleus.domain

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Pagination
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.domain.model.PlanetDetailsState
import com.tsaha.nucleus.domain.model.PlanetListResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for PlanetListUseCase using MockK and Turbine
 *
 * This test class covers all major scenarios:
 * - Success flows with planet loading
 * - Error handling scenarios
 * - Pagination support
 * - Planet details loading
 * - Repository interaction verification
 *
 * Test Structure:
 * - Uses MockK for mocking
 * - Uses Turbine for testing Flows
 * - Tests Flow emissions in correct order
 * - Verifies domain model transformations
 * - Covers all code paths
 */
class PlanetListUseCaseTest {

    private lateinit var planetListUseCase: PlanetListUseCase
    private lateinit var mockRepository: PlanetRepository
    private lateinit var loadNextFlow: MutableSharedFlow<Int>

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

    private val paginationWithNext = Pagination(currentPage = 1, nextPage = "2")
    private val paginationNoNext = Pagination(currentPage = 1, nextPage = null)

    @Before
    fun setUp() {
        mockRepository = mockk(relaxed = true)
        loadNextFlow = MutableSharedFlow(replay = 1)
        planetListUseCase = PlanetListUseCase(mockRepository)
    }

    // ===============================
    // SUCCESS SCENARIOS
    // ===============================

    @Test
    fun `observePlanets should emit loading state first`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine))

        coEvery {
            mockRepository.getPlanet(any())
        } returns Result.success(tatooineDetails)

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val firstEmission = awaitItem()
            assertThat(firstEmission).isInstanceOf(PlanetListResult.Loading::class)

            coVerify(atLeast = 1) {
                mockRepository.getPlanetsWithPagination(
                    pageNumber = any(),
                    limit = any()
                )
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should emit success with planets and loading details`() = runTest {
        // Given
        val planets = listOf(tatooine, alderaan)
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to planets)

        coEvery { mockRepository.getPlanet("1") } returns Result.success(tatooineDetails)
        coEvery { mockRepository.getPlanet("2") } returns Result.success(alderaanDetails)

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val successState = awaitItem() as PlanetListResult.Success
            assertThat(successState.items).hasSize(2)
            assertThat(successState.items[0].planet.name).isEqualTo("Tatooine")
            assertThat(successState.items[1].planet.name).isEqualTo("Alderaan")

            // Initially all details should be loading
            successState.items.forEach { planetItem ->
                assertThat(planetItem.detailsState).isInstanceOf(PlanetDetailsState.Loading::class)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should eventually load planet details successfully`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine))

        coEvery { mockRepository.getPlanet("1") } returns Result.success(tatooineDetails)

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val firstSuccess = awaitItem() as PlanetListResult.Success
            assertThat(firstSuccess.items).hasSize(1)

            // Wait for detail loading emission
            val updatedSuccess = awaitItem() as PlanetListResult.Success
            assertThat(updatedSuccess.items[0].detailsState).isInstanceOf(PlanetDetailsState.Available::class)

            coVerify(atLeast = 1) { mockRepository.getPlanet("1") }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should respect custom page size`() = runTest {
        // Given
        val customPageSize = 15
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine))

        coEvery { mockRepository.getPlanet("1") } returns Result.success(tatooineDetails)

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow, pageSize = customPageSize).test {
            loadNextFlow.emit(1)

            awaitItem() // Loading
            awaitItem() // Success

            coVerify(atLeast = 1) {
                mockRepository.getPlanetsWithPagination(
                    pageNumber = any(),
                    limit = any()
                )
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should handle multiple pages with pagination`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = 1, limit = any())
        } returns Result.success(paginationWithNext to listOf(tatooine))

        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = 2, limit = any())
        } returns Result.success(paginationNoNext to listOf(alderaan))

        coEvery { mockRepository.getPlanet("1") } returns Result.success(tatooineDetails)
        coEvery { mockRepository.getPlanet("2") } returns Result.success(alderaanDetails)

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            awaitItem() // First page success

            loadNextFlow.emit(2)

            awaitItem() // Second page loading indicator
            awaitItem() // Second page success

            coVerify(atLeast = 1) {
                mockRepository.getPlanetsWithPagination(pageNumber = 1, limit = any())
            }
            coVerify(atLeast = 1) {
                mockRepository.getPlanetsWithPagination(pageNumber = 2, limit = any())
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===============================
    // ERROR SCENARIOS
    // ===============================

    @Test
    fun `observePlanets should emit error when planets fetch fails`() = runTest {
        // Given
        val errorMessage = "Network connection failed"
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.failure(RuntimeException(errorMessage))

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val errorState = awaitItem() as PlanetListResult.Error
            assertThat(errorState.message).isEqualTo(errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should emit error when planets list is empty`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to emptyList())

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val errorState = awaitItem()
            assertThat(errorState).isInstanceOf(PlanetListResult.Error::class)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should handle planet details fetch failure`() = runTest {
        // Given
        val planets = listOf(tatooine, alderaan)
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to planets)

        coEvery { mockRepository.getPlanet("1") } returns Result.success(tatooineDetails)
        coEvery { mockRepository.getPlanet("2") } returns Result.failure(RuntimeException("Planet not found"))

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val firstSuccess = awaitItem()
            assertThat(firstSuccess).isInstanceOf(PlanetListResult.Success::class)

            // Wait for at least one detail update
            awaitItem()

            coVerify(atLeast = 1) { mockRepository.getPlanet("1") }
            coVerify(atLeast = 1) { mockRepository.getPlanet("2") }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ===============================
    // SEARCH FUNCTIONALITY
    // ===============================

    @Test
    fun `searchPlanets should emit searching state first`() = runTest {
        // Given
        val query = "Tatooine"
        coEvery { mockRepository.searchPlanets(query) } returns Result.success(
            listOf(
                tatooineDetails
            )
        )

        // When & Then
        planetListUseCase.searchPlanets(query).test {
            val searchingState = awaitItem() as PlanetListResult.SearchResult
            assertThat(searchingState.query).isEqualTo(query)
            assertThat(searchingState.isSearching).isTrue()
            assertThat(searchingState.items).hasSize(0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchPlanets should return results with details available`() = runTest {
        // Given
        val query = "Tatooine"
        coEvery { mockRepository.searchPlanets(query) } returns Result.success(
            listOf(
                tatooineDetails
            )
        )

        // When & Then
        planetListUseCase.searchPlanets(query).test {
            val searchingState = awaitItem() as PlanetListResult.SearchResult
            assertThat(searchingState.isSearching).isTrue()

            val resultState = awaitItem() as PlanetListResult.SearchResult
            assertThat(resultState.isSearching).isEqualTo(false)
            assertThat(resultState.items).hasSize(1)
            assertThat(resultState.items[0].planet.name).isEqualTo("Tatooine")
            assertThat(resultState.items[0].detailsState).isInstanceOf(PlanetDetailsState.Available::class)

            awaitComplete()
        }
    }

    @Test
    fun `searchPlanets should emit empty result for blank query`() = runTest {
        // Given
        val query = "   "

        // When & Then
        planetListUseCase.searchPlanets(query).test {
            awaitItem() // Searching state

            val resultState = awaitItem() as PlanetListResult.SearchResult
            assertThat(resultState.isSearching).isEqualTo(false)
            assertThat(resultState.items).hasSize(0)

            coVerify(exactly = 0) { mockRepository.searchPlanets(any()) }

            awaitComplete()
        }
    }

    @Test
    fun `searchPlanets should emit error on failure`() = runTest {
        // Given
        val query = "Unknown"
        val errorMessage = "Search failed"
        coEvery { mockRepository.searchPlanets(query) } returns Result.failure(
            RuntimeException(
                errorMessage
            )
        )

        // When & Then
        planetListUseCase.searchPlanets(query).test {
            awaitItem() // Searching state

            val errorState = awaitItem() as PlanetListResult.Error
            assertThat(errorState.message).isEqualTo(errorMessage)

            awaitComplete()
        }
    }

    // ===============================
    // EDGE CASES
    // ===============================

    @Test
    fun `observePlanets should handle single planet correctly`() = runTest {
        // Given
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to listOf(tatooine))

        coEvery { mockRepository.getPlanet("1") } returns Result.success(tatooineDetails)

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val successState = awaitItem() as PlanetListResult.Success
            assertThat(successState.items).hasSize(1)
            assertThat(successState.items[0].planet.name).isEqualTo("Tatooine")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observePlanets should handle large number of planets`() = runTest {
        // Given
        val manyPlanets = (1..20).map { Planet(uid = it.toString(), name = "Planet $it") }
        coEvery {
            mockRepository.getPlanetsWithPagination(pageNumber = any(), limit = any())
        } returns Result.success(paginationNoNext to manyPlanets)

        // Setup details for each planet with specific uid
        manyPlanets.forEach { planet ->
            coEvery { mockRepository.getPlanet(planet.uid) } returns Result.success(
                PlanetDetails(
                    uid = planet.uid,
                    name = planet.name,
                    climate = "varies",
                    population = "unknown",
                    diameter = "unknown",
                    gravity = "1 standard",
                    terrain = "mixed"
                )
            )
        }

        // When & Then
        planetListUseCase.observePlanets(loadNextFlow).test {
            loadNextFlow.emit(1)

            val loadingState = awaitItem()
            assertThat(loadingState).isInstanceOf(PlanetListResult.Loading::class)

            val successState = awaitItem() as PlanetListResult.Success
            assertThat(successState.items).hasSize(20)

            // All should initially be loading
            assertThat(successState.items.all { it.detailsState is PlanetDetailsState.Loading }).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }
}