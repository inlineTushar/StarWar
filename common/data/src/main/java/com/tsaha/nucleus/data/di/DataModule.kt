package com.tsaha.nucleus.data.di

import com.tsaha.nucleus.data.api.PlanetApi
import com.tsaha.nucleus.data.api.PlanetApiImpl
import com.tsaha.nucleus.data.datasource.InMemoryPlanetDataSource
import com.tsaha.nucleus.data.datasource.PlanetDataSource
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.data.repository.PlanetRepositoryImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Comprehensive Koin module for data layer dependencies
 * Includes HTTP client, API implementations, datasources, and repositories
 */
val dataModule = module {
    includes(httpModule)
    singleOf(::PlanetApiImpl) { bind<PlanetApi>() }
    singleOf(::InMemoryPlanetDataSource) { bind<PlanetDataSource>() }
    singleOf(::PlanetRepositoryImpl) { bind<PlanetRepository>() }
}