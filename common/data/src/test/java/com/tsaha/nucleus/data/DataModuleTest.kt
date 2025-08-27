package com.tsaha.nucleus.data

import com.tsaha.nucleus.data.api.PlanetApi
import com.tsaha.nucleus.data.di.dataModule
import com.tsaha.nucleus.data.repository.PlanetRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * Data layer unit test to ensure all dependencies are properly configured and can be resolved.
 * Updated to test interface-based repository injection (PlanetRepository interface -> PlanetRepositoryImpl).
 */
class DataModuleTest : KoinTest {

    @Before
    fun setUp() {
        // Setup for unit tests
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify httpClient can be created and configured properly`() {
        // Test basic Koin module functionality by creating an HttpClient
        val testModule = module {
            single<HttpClient> {
                // Basic HttpClient configuration
                HttpClient()
            }
        }

        startKoin {
            modules(testModule)
        }

        val httpClient: HttpClient by inject()
        assertNotNull(httpClient)
        httpClient.close()
    }

    @Test
    fun `test basic http client creation`() {
        val httpClient = HttpClient()
        assertNotNull(httpClient)
        httpClient.close()
    }

    @Test
    fun `test safe api call with success`() = runTest {
        val result = runCatching { "success" }

        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun `test safe api call with failure`() = runTest {
        val exception = RuntimeException("Test exception")

        val result = runCatching { throw exception }

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    // TODO: Uncomment and implement when data module components are ready for integration testing
    @Test
    fun `verify dataModule provides all required dependencies`() {
        // Start Koin with dataModule
        startKoin {
            modules(dataModule)
        }

        val httpClient: HttpClient by inject()
        val planetApi: PlanetApi by inject() // Interface injection
        val planetRepository: PlanetRepository by inject() // Interface injection

        assertNotNull(httpClient)
        assertNotNull(planetApi)
        assertNotNull(planetRepository)

        httpClient.close()
    }

    @Test
    fun `verify planet repository integration with dependencies`() = runTest {
        startKoin {
            modules(dataModule)
        }

        val planetRepository: PlanetRepository by inject()

        // Test that the repository was properly constructed with dependencies
        assertNotNull(planetRepository)

        // You could test actual functionality here with a mock server
        // val result = planetRepository.getPlanets(pageNumber = 1, limit = 10)
        // assertTrue(result.isSuccess || result.isFailure) // Either is valid for network calls
    }

    @Test
    fun `verify planet repository parameter validation`() = runTest {
        startKoin {
            modules(dataModule)
        }

        val planetRepository: PlanetRepository by inject()

        // Test parameter validation
        val invalidPageResult = planetRepository.getPlanets(pageNumber = 0, limit = 10)
        assertTrue(invalidPageResult.isFailure)

        val invalidLimitResult = planetRepository.getPlanets(pageNumber = 1, limit = -1)
        assertTrue(invalidLimitResult.isFailure)
    }
}