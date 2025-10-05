package com.tsaha.nucleus.data.datasource.local.runtimememory

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.tsaha.nucleus.data.datasource.local.runtimememory.model.PlanetDetailsMemoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for InMemoryPlanetDataSource
 *
 * Tests cover:
 * - Basic CRUD operations (Create, Read, Update, Delete)
 * - Thread safety and concurrent access
 * - Edge cases (empty data, null handling, duplicates)
 * - Bulk operations
 * - Data integrity
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryPlanetDataSourceTest {

    private lateinit var dataSource: InMemoryPlanetDataSource

    // Test Data
    private val testPlanet1 = PlanetDetailsMemoryModel(
        uid = "1",
        name = "Tatooine",
        climate = "arid",
        population = "200000",
        diameter = "10465",
        gravity = "1 standard",
        terrain = "desert"
    )

    private val testPlanet2 = PlanetDetailsMemoryModel(
        uid = "2",
        name = "Alderaan",
        climate = "temperate",
        population = "2000000000",
        diameter = "12500",
        gravity = "1 standard",
        terrain = "grasslands, mountains"
    )

    private val testPlanet3 = PlanetDetailsMemoryModel(
        uid = "3",
        name = "Hoth",
        climate = "frozen",
        population = "unknown",
        diameter = "7200",
        gravity = "1.1 standard",
        terrain = "tundra, ice caves, mountain ranges"
    )

    @Before
    fun setUp() {
        dataSource = InMemoryPlanetDataSource()
    }

    // ===============================
    // BASIC STORAGE TESTS
    // ===============================

    @Test
    fun `storePlanet should store planet successfully`() = runTest {
        // When
        dataSource.storePlanet(testPlanet1)

        // Then
        val retrieved = dataSource.getPlanet(testPlanet1.uid)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved).isEqualTo(testPlanet1)
    }

    @Test
    fun `storePlanet should overwrite existing planet with same UID`() = runTest {
        // Given - Store initial planet
        dataSource.storePlanet(testPlanet1)

        // When - Store planet with same UID but different data
        val updatedPlanet = testPlanet1.copy(name = "Updated Tatooine", climate = "extremely arid")
        dataSource.storePlanet(updatedPlanet)

        // Then - Should have updated data
        val retrieved = dataSource.getPlanet(testPlanet1.uid)
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Updated Tatooine")
        assertThat(retrieved?.climate).isEqualTo("extremely arid")
    }

    @Test
    fun `storePlanet should store multiple different planets`() = runTest {
        // When
        dataSource.storePlanet(testPlanet1)
        dataSource.storePlanet(testPlanet2)
        dataSource.storePlanet(testPlanet3)

        // Then
        val planet1 = dataSource.getPlanet(testPlanet1.uid)
        val planet2 = dataSource.getPlanet(testPlanet2.uid)
        val planet3 = dataSource.getPlanet(testPlanet3.uid)

        assertThat(planet1).isEqualTo(testPlanet1)
        assertThat(planet2).isEqualTo(testPlanet2)
        assertThat(planet3).isEqualTo(testPlanet3)
    }

    // ===============================
    // RETRIEVAL TESTS
    // ===============================

    @Test
    fun `getPlanet should return null for non-existent planet`() = runTest {
        // When
        val result = dataSource.getPlanet("non-existent-id")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `getPlanet should return correct planet by UID`() = runTest {
        // Given
        dataSource.storePlanet(testPlanet1)
        dataSource.storePlanet(testPlanet2)

        // When
        val planet1 = dataSource.getPlanet("1")
        val planet2 = dataSource.getPlanet("2")

        // Then
        assertThat(planet1).isEqualTo(testPlanet1)
        assertThat(planet2).isEqualTo(testPlanet2)
    }

    @Test
    fun `getPlanet should return null after planet is cleared`() = runTest {
        // Given
        dataSource.storePlanet(testPlanet1)
        dataSource.clearAll()

        // When
        val result = dataSource.getPlanet(testPlanet1.uid)

        // Then
        assertThat(result).isNull()
    }

    // ===============================
    // BULK OPERATIONS TESTS
    // ===============================

    @Test
    fun `storePlanets should store multiple planets at once`() = runTest {
        // Given
        val planetList = listOf(testPlanet1, testPlanet2, testPlanet3)

        // When
        dataSource.storePlanets(planetList)

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(3)
        assertThat(allPlanets).containsExactlyInAnyOrder(testPlanet1, testPlanet2, testPlanet3)
    }

    @Test
    fun `storePlanets should handle empty list`() = runTest {
        // Given
        val emptyList = emptyList<PlanetDetailsMemoryModel>()

        // When
        dataSource.storePlanets(emptyList)

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).isEmpty()
    }

    @Test
    fun `storePlanets should overwrite existing planets with same UIDs`() = runTest {
        // Given - Store initial planets
        dataSource.storePlanets(listOf(testPlanet1, testPlanet2))

        // When - Store overlapping planets with updates
        val updatedPlanet1 = testPlanet1.copy(name = "Updated Tatooine")
        val newPlanet = testPlanet3
        dataSource.storePlanets(listOf(updatedPlanet1, newPlanet))

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(3)

        val planet1 = dataSource.getPlanet("1")
        assertThat(planet1?.name).isEqualTo("Updated Tatooine")

        val planet2 = dataSource.getPlanet("2")
        assertThat(planet2).isEqualTo(testPlanet2)

        val planet3 = dataSource.getPlanet("3")
        assertThat(planet3).isEqualTo(testPlanet3)
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
        dataSource.storePlanet(testPlanet1)
        dataSource.storePlanet(testPlanet2)
        dataSource.storePlanet(testPlanet3)

        // When
        val allPlanets = dataSource.getAllPlanets()

        // Then
        assertThat(allPlanets).hasSize(3)
        assertThat(allPlanets).containsExactlyInAnyOrder(testPlanet1, testPlanet2, testPlanet3)
    }

    // ===============================
    // CLEAR OPERATIONS TESTS
    // ===============================

    @Test
    fun `clearAll should remove all planets`() = runTest {
        // Given
        dataSource.storePlanet(testPlanet1)
        dataSource.storePlanet(testPlanet2)
        dataSource.storePlanet(testPlanet3)

        // When
        dataSource.clearAll()

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).isEmpty()

        assertThat(dataSource.getPlanet("1")).isNull()
        assertThat(dataSource.getPlanet("2")).isNull()
        assertThat(dataSource.getPlanet("3")).isNull()
    }

    @Test
    fun `clearAll should work on empty data source`() = runTest {
        // When
        dataSource.clearAll()

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).isEmpty()
    }

    @Test
    fun `clearAll should allow storing new planets after clear`() = runTest {
        // Given
        dataSource.storePlanet(testPlanet1)
        dataSource.clearAll()

        // When
        dataSource.storePlanet(testPlanet2)

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(1)
        assertThat(allPlanets.first()).isEqualTo(testPlanet2)
        assertThat(dataSource.getPlanet("1")).isNull()
        assertThat(dataSource.getPlanet("2")).isNotNull()
    }

    // ===============================
    // EDGE CASES TESTS
    // ===============================

    @Test
    fun `should handle planet with empty string values`() = runTest {
        // Given
        val emptyPlanet = PlanetDetailsMemoryModel(
            uid = "empty",
            name = "",
            climate = "",
            population = "",
            diameter = "",
            gravity = "",
            terrain = ""
        )

        // When
        dataSource.storePlanet(emptyPlanet)

        // Then
        val retrieved = dataSource.getPlanet("empty")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved).isEqualTo(emptyPlanet)
    }

    @Test
    fun `should handle planet with very long string values`() = runTest {
        // Given
        val longString = "a".repeat(10000)
        val longPlanet = PlanetDetailsMemoryModel(
            uid = "long",
            name = longString,
            climate = longString,
            population = longString,
            diameter = longString,
            gravity = longString,
            terrain = longString
        )

        // When
        dataSource.storePlanet(longPlanet)

        // Then
        val retrieved = dataSource.getPlanet("long")
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name?.length).isEqualTo(10000)
    }

    @Test
    fun `should handle planet with special characters in UID`() = runTest {
        // Given
        val specialUids = listOf(
            "planet-123",
            "planet_456",
            "planet.789",
            "planet!@#",
            "PLANET-ABC",
            "planet with spaces"
        )

        // When
        specialUids.forEachIndexed { index, uid ->
            val planet = testPlanet1.copy(uid = uid, name = "Planet $index")
            dataSource.storePlanet(planet)
        }

        // Then
        specialUids.forEach { uid ->
            val retrieved = dataSource.getPlanet(uid)
            assertThat(retrieved).isNotNull()
            assertThat(retrieved?.uid).isEqualTo(uid)
        }
    }

    @Test
    fun `should handle large number of planets`() = runTest {
        // Given
        val largePlanetList = (1..1000).map { index ->
            PlanetDetailsMemoryModel(
                uid = "planet-$index",
                name = "Planet $index",
                climate = "climate-$index",
                population = "$index",
                diameter = "${index * 1000}",
                gravity = "1.${index % 10}",
                terrain = "terrain-$index"
            )
        }

        // When
        dataSource.storePlanets(largePlanetList)

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(1000)

        val firstPlanet = dataSource.getPlanet("planet-1")
        val middlePlanet = dataSource.getPlanet("planet-500")
        val lastPlanet = dataSource.getPlanet("planet-1000")

        assertThat(firstPlanet).isNotNull()
        assertThat(middlePlanet).isNotNull()
        assertThat(lastPlanet).isNotNull()
    }

    // ===============================
    // CONCURRENT ACCESS TESTS
    // ===============================

    @Test
    fun `should handle concurrent writes to different planets`() = runTest {
        // When - Concurrent writes to different planets
        val job1 = launch(Dispatchers.Default) {
            repeat(100) {
                dataSource.storePlanet(testPlanet1.copy(uid = "1-$it"))
            }
        }

        val job2 = launch(Dispatchers.Default) {
            repeat(100) {
                dataSource.storePlanet(testPlanet2.copy(uid = "2-$it"))
            }
        }

        val job3 = launch(Dispatchers.Default) {
            repeat(100) {
                dataSource.storePlanet(testPlanet3.copy(uid = "3-$it"))
            }
        }

        job1.join()
        job2.join()
        job3.join()

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(300)
    }

    @Test
    fun `should handle concurrent writes to same planet`() = runTest {
        // When - Multiple concurrent updates to same planet
        val jobs = (1..100).map { index ->
            launch(Dispatchers.Default) {
                val updatedPlanet = testPlanet1.copy(name = "Version $index")
                dataSource.storePlanet(updatedPlanet)
            }
        }

        jobs.forEach { it.join() }

        // Then - Should have exactly one planet with one of the names
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(1)

        val planet = dataSource.getPlanet("1")
        assertThat(planet).isNotNull()
        assertThat(planet?.uid).isEqualTo("1")
        assertThat(planet?.name).isNotNull()
    }

    @Test
    fun `should handle concurrent reads and writes`() = runTest {
        // Given
        dataSource.storePlanet(testPlanet1)

        // When - Concurrent reads and writes
        val writeJobs = (1..50).map { index ->
            launch(Dispatchers.Default) {
                dataSource.storePlanet(testPlanet1.copy(name = "Version $index"))
            }
        }

        val readJobs = (1..50).map {
            launch(Dispatchers.Default) {
                val planet = dataSource.getPlanet("1")
                assertThat(planet).isNotNull()
            }
        }

        (writeJobs + readJobs).forEach { it.join() }

        // Then - Should still have one planet and no errors
        val planet = dataSource.getPlanet("1")
        assertThat(planet).isNotNull()
    }

    @Test
    fun `should handle concurrent bulk operations`() = runTest {
        // Given
        val batch1 = (1..50).map {
            PlanetDetailsMemoryModel(
                "$it",
                "P$it",
                "C$it",
                "Pop$it",
                "D$it",
                "G$it",
                "T$it"
            )
        }
        val batch2 = (51..100).map {
            PlanetDetailsMemoryModel(
                "$it",
                "P$it",
                "C$it",
                "Pop$it",
                "D$it",
                "G$it",
                "T$it"
            )
        }

        // When - Concurrent bulk stores
        val job1 = launch(Dispatchers.Default) {
            dataSource.storePlanets(batch1)
        }

        val job2 = launch(Dispatchers.Default) {
            dataSource.storePlanets(batch2)
        }

        job1.join()
        job2.join()

        // Then
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).hasSize(100)
    }

    @Test
    fun `should handle concurrent getAllPlanets calls`() = runTest {
        // Given
        dataSource.storePlanets(listOf(testPlanet1, testPlanet2, testPlanet3))

        // When - Multiple concurrent getAllPlanets calls
        val results = mutableListOf<List<PlanetDetailsMemoryModel>>()
        val jobs = (1..20).map {
            launch(Dispatchers.Default) {
                val planets = dataSource.getAllPlanets()
                synchronized(results) {
                    results.add(planets)
                }
            }
        }

        jobs.forEach { it.join() }

        // Then - All calls should return same count
        assertThat(results).hasSize(20)
        results.forEach { planetList ->
            assertThat(planetList).hasSize(3)
        }
    }

    @Test
    fun `should handle concurrent clear operations`() = runTest {
        // Given
        dataSource.storePlanets(listOf(testPlanet1, testPlanet2, testPlanet3))

        // When - Concurrent clears
        val jobs = (1..10).map {
            launch(Dispatchers.Default) {
                dataSource.clearAll()
            }
        }

        jobs.forEach { it.join() }

        // Then - Should be empty
        val allPlanets = dataSource.getAllPlanets()
        assertThat(allPlanets).isEmpty()
    }

    // ===============================
    // DATA INTEGRITY TESTS
    // ===============================

    @Test
    fun `stored data should remain unchanged after retrieval`() = runTest {
        // Given
        val originalPlanet = testPlanet1.copy()
        dataSource.storePlanet(originalPlanet)

        // When - Retrieve multiple times
        val retrieval1 = dataSource.getPlanet(originalPlanet.uid)
        val retrieval2 = dataSource.getPlanet(originalPlanet.uid)

        // Then - Data should be identical
        assertThat(retrieval1).isEqualTo(originalPlanet)
        assertThat(retrieval2).isEqualTo(originalPlanet)
        assertThat(retrieval1).isEqualTo(retrieval2)
    }

    @Test
    fun `modifying retrieved planet should not affect stored data`() = runTest {
        // Given
        dataSource.storePlanet(testPlanet1)

        // When - Retrieve and modify
        val retrieved = dataSource.getPlanet(testPlanet1.uid)
        val modified = retrieved?.copy(name = "Modified Name")

        // Then - Original stored data unchanged
        val storedPlanet = dataSource.getPlanet(testPlanet1.uid)
        assertThat(storedPlanet?.name).isEqualTo("Tatooine")
        assertThat(storedPlanet?.name).isEqualTo(testPlanet1.name)
    }
}