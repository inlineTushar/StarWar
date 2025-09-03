package com.tsaha.nucleus.data.datasource

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.tsaha.nucleus.data.model.Planet
import com.tsaha.nucleus.data.model.PlanetDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

/**
 * Comprehensive test suite for InMemoryPlanetDataSource
 *
 * Tests cover:
 * - Basic CRUD operations
 * - Thread safety and concurrent access
 * - Input validation and error handling
 * - Batch operations and performance
 * - Search and utility functions
 * - Edge cases and boundary conditions
 */
class InMemoryPlanetDetailsDataSourceTest {

    private lateinit var dataSource: InMemoryPlanetDataSource

    private val samplePlanet1 = PlanetDetails(
        planet = Planet(uid = "planet1", name = "Tatooine"),
        climate = "arid",
        population = "200000",
        diameter = "10465",
        gravity = "1 standard",
        terrain = "desert"
    )

    private val samplePlanet2 = PlanetDetails(
        planet = Planet(uid = "planet2", name = "Alderaan"),
        climate = "temperate",
        population = "2000000000",
        diameter = "12500",
        gravity = "1 standard",
        terrain = "grasslands, mountains"
    )

    private val samplePlanet3 = PlanetDetails(
        planet = Planet(uid = "planet3", name = "Coruscant"),
        climate = "temperate",
        population = "1000000000000",
        diameter = "12240",
        gravity = "1 standard",
        terrain = "cityscape"
    )

    @Before
    fun setup() {
        dataSource = InMemoryPlanetDataSource()
    }

    // ================================
    // BASIC CRUD OPERATIONS
    // ================================

    @Test
    fun `storePlanet should store planet successfully`() = runTest {
        // When
        dataSource.storePlanet(samplePlanet1)

        // Then
        val retrieved = dataSource.getPlanet("planet1")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Tatooine")
        assertThat(retrieved?.climate).isEqualTo("arid")
    }

    @Test
    fun `getPlanet should return null for non-existent planet`() = runTest {
        // When
        val result = dataSource.getPlanet("non-existent")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `storePlanet should overwrite existing planet`() = runTest {
        // Given
        dataSource.storePlanet(samplePlanet1)
        val updatedPlanet = samplePlanet1.copy(climate = "updated climate")

        // When
        dataSource.storePlanet(updatedPlanet)

        // Then
        val retrieved = dataSource.getPlanet("planet1")
        assertThat(retrieved?.climate).isEqualTo("updated climate")
    }

    @Test
    fun `getAllPlanets should return empty list when no planets stored`() = runTest {
        // When
        val result = dataSource.getAllPlanets()

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `getAllPlanets should return all stored planets`() = runTest {
        // Given
        dataSource.storePlanet(samplePlanet1)
        dataSource.storePlanet(samplePlanet2)

        // When
        val result = dataSource.getAllPlanets()

        // Then
        assertThat(result).hasSize(2)
        val uids = result.map { it.uid }
        assertThat(uids).contains("planet1")
        assertThat(uids).contains("planet2")
    }

    @Test
    fun `clearAll should remove all planets`() = runTest {
        // Given
        dataSource.storePlanet(samplePlanet1)
        dataSource.storePlanet(samplePlanet2)

        // When
        dataSource.clearAll()

        // Then
        assertThat(dataSource.getAllPlanets()).isEmpty()
        assertThat(dataSource.getPlanetCount()).isEqualTo(0)
    }

    // ================================
    // BATCH OPERATIONS
    // ================================

    @Test
    fun `storePlanets should store multiple planets atomically`() = runTest {
        // Given
        val planets = listOf(samplePlanet1, samplePlanet2, samplePlanet3)

        // When
        dataSource.storePlanets(planets)

        // Then
        assertThat(dataSource.getPlanetCount()).isEqualTo(3)
        assertThat(dataSource.containsPlanet("planet1")).isTrue()
        assertThat(dataSource.containsPlanet("planet2")).isTrue()
        assertThat(dataSource.containsPlanet("planet3")).isTrue()
    }

    @Test
    fun `getPlanetsInBatches should return planets in correct batch sizes`() = runTest {
        // Given
        val planets = listOf(samplePlanet1, samplePlanet2, samplePlanet3)
        dataSource.storePlanets(planets)

        // When
        val batches = dataSource.getPlanetsInBatches(2).toList()

        // Then
        assertThat(batches).hasSize(2)
        assertThat(batches[0]).hasSize(2)
        assertThat(batches[1]).hasSize(1)
    }

    // ================================
    // UTILITY METHODS
    // ================================

    @Test
    fun `getPlanetCount should return correct count`() = runTest {
        // Given
        assertThat(dataSource.getPlanetCount()).isEqualTo(0)

        // When
        dataSource.storePlanet(samplePlanet1)
        dataSource.storePlanet(samplePlanet2)

        // Then
        assertThat(dataSource.getPlanetCount()).isEqualTo(2)
    }

    @Test
    fun `containsPlanet should return correct existence status`() = runTest {
        // Given
        dataSource.storePlanet(samplePlanet1)

        // When & Then
        assertThat(dataSource.containsPlanet("planet1")).isTrue()
        assertThat(dataSource.containsPlanet("non-existent")).isFalse()
    }

    @Test
    fun `removePlanet should remove and return planet`() = runTest {
        // Given
        dataSource.storePlanet(samplePlanet1)
        dataSource.storePlanet(samplePlanet2)

        // When
        val removed = dataSource.removePlanet("planet1")

        // Then
        assertThat(removed).isNotNull()
        assertThat(removed?.uid).isEqualTo("planet1")
        assertThat(dataSource.containsPlanet("planet1")).isFalse()
        assertThat(dataSource.containsPlanet("planet2")).isTrue()
        assertThat(dataSource.getPlanetCount()).isEqualTo(1)
    }

    @Test
    fun `removePlanet should return null for non-existent planet`() = runTest {
        // When
        val removed = dataSource.removePlanet("non-existent")

        // Then
        assertThat(removed).isNull()
    }

    @Test
    fun `searchPlanetsByName should find planets case-insensitively`() = runTest {
        // Given
        dataSource.storePlanets(listOf(samplePlanet1, samplePlanet2, samplePlanet3))

        // When
        val results1 = dataSource.searchPlanetsByName("tatooine")
        val results2 = dataSource.searchPlanetsByName("ALDERAAN")
        val results3 = dataSource.searchPlanetsByName("coru")
        val results4 = dataSource.searchPlanetsByName("nonexistent")

        // Then
        assertThat(results1).hasSize(1)
        assertThat(results1.first().name).isEqualTo("Tatooine")

        assertThat(results2).hasSize(1)
        assertThat(results2.first().name).isEqualTo("Alderaan")

        assertThat(results3).hasSize(1)
        assertThat(results3.first().name).isEqualTo("Coruscant")

        assertThat(results4).isEmpty()
    }

    @Test
    fun `updateOrStorePlanet should return null for new planet`() = runTest {
        // When
        val previous = dataSource.updateOrStorePlanet(samplePlanet1)

        // Then
        assertThat(previous).isNull()
        assertThat(dataSource.containsPlanet("planet1")).isTrue()
    }

    @Test
    fun `updateOrStorePlanet should return previous planet when updating`() = runTest {
        // Given
        dataSource.storePlanet(samplePlanet1)
        val updatedPlanet = samplePlanet1.copy(climate = "updated")

        // When
        val previous = dataSource.updateOrStorePlanet(updatedPlanet)

        // Then
        assertThat(previous).isNotNull()
        assertThat(previous?.climate).isEqualTo("arid")
        assertThat(dataSource.getPlanet("planet1")?.climate).isEqualTo("updated")
    }

    // ================================
    // INPUT VALIDATION
    // ================================

    @Test
    fun `storePlanet should throw exception for blank UID`() = runTest {
        // Given
        val invalidPlanet = samplePlanet1.copy(planet = Planet(uid = "", name = "Test"))

        // When & Then
        assertFailsWith<IllegalArgumentException> {
            dataSource.storePlanet(invalidPlanet)
        }
    }

    @Test
    fun `getPlanet should throw exception for blank ID`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            dataSource.getPlanet("")
        }
    }

    @Test
    fun `storePlanets should throw exception for empty list`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            dataSource.storePlanets(emptyList())
        }
    }

    @Test
    fun `containsPlanet should throw exception for blank ID`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            dataSource.containsPlanet("")
        }
    }

    @Test
    fun `removePlanet should throw exception for blank ID`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            dataSource.removePlanet("")
        }
    }

    @Test
    fun `searchPlanetsByName should throw exception for blank pattern`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            dataSource.searchPlanetsByName("")
        }
    }

    @Test
    fun `updateOrStorePlanet should throw exception for blank UID`() = runTest {
        val invalidPlanet = samplePlanet1.copy(planet = Planet(uid = "", name = "Test"))

        assertFailsWith<IllegalArgumentException> {
            dataSource.updateOrStorePlanet(invalidPlanet)
        }
    }

    @Test
    fun `getPlanetsInBatches should throw exception for invalid batch size`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            dataSource.getPlanetsInBatches(0)
        }

        assertFailsWith<IllegalArgumentException> {
            dataSource.getPlanetsInBatches(-1)
        }
    }

    // ================================
    // CONCURRENT ACCESS TESTS
    // ================================

    @Test
    fun `concurrent read and write operations should be thread-safe`() = runTest {
        // Given
        val planets = (1..100).map { i ->
            PlanetDetails(
                planet = Planet(uid = "planet$i", name = "Planet $i"),
                climate = "climate$i",
                population = "${i * 1000}",
                diameter = "${i * 100}",
                gravity = "1g",
                terrain = "terrain$i"
            )
        }

        // When - Concurrent operations
        val writeOperations = planets.map { planet ->
            async { dataSource.storePlanet(planet) }
        }

        val readOperations = (1..50).map { i ->
            async { dataSource.getPlanet("planet$i") }
        }

        val utilityOperations = (1..20).map { i ->
            async { dataSource.containsPlanet("planet$i") }
        }

        // Wait for all operations to complete
        writeOperations.awaitAll()
        val readResults = readOperations.awaitAll()
        val utilityResults = utilityOperations.awaitAll()

        // Then
        assertThat(dataSource.getPlanetCount()).isEqualTo(100)

        // Some reads should succeed (those that happened after writes)
        val successfulReads = readResults.filterNotNull()
        assertThat(successfulReads.size).isEqualTo(50) // All should succeed since we await writes first

        // Utility operations should work correctly
        assertThat(utilityResults).contains(true)
    }

    @Test
    fun `concurrent batch operations should maintain data integrity`() = runTest {
        // Given
        val batch1 = listOf(samplePlanet1, samplePlanet2)
        val batch2 = listOf(samplePlanet3)

        // When - Concurrent batch operations
        val operations = listOf(
            async { dataSource.storePlanets(batch1) },
            async { dataSource.storePlanets(batch2) },
            async { dataSource.getAllPlanets() },
            async { dataSource.getPlanetCount() }
        )

        operations.awaitAll()

        // Then
        assertThat(dataSource.getPlanetCount()).isEqualTo(3)
        assertThat(dataSource.containsPlanet("planet1")).isTrue()
        assertThat(dataSource.containsPlanet("planet2")).isTrue()
        assertThat(dataSource.containsPlanet("planet3")).isTrue()
    }
}
