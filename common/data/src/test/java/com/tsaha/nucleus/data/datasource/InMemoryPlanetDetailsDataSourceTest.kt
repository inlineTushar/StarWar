package com.tsaha.nucleus.data.datasource

import com.tsaha.nucleus.data.model.PlanetDetailsApiModel
import org.junit.Before

class InMemoryPlanetDetailsDataSourceTest {

    private lateinit var dataSource: InMemoryPlanetDataSource

    private val samplePlanet1 = PlanetDetailsApiModel(
        uid = "planet1",
        name = "Tatooine",
        climate = "arid",
        population = "200000",
        diameter = "10465",
        gravity = "1 standard",
        terrain = "desert"
    )

    private val samplePlanet2 = PlanetDetailsApiModel(
        uid = "planet2",
        name = "Alderaan",
        climate = "temperate",
        population = "2000000000",
        diameter = "12500",
        gravity = "1 standard",
        terrain = "grasslands, mountains"
    )

    @Before
    fun setup() {
        dataSource = InMemoryPlanetDataSource()
    }
}
