package com.tsaha.nucleus.data.di

import com.tsaha.nucleus.data.datasource.remote.api.PlanetApi
import com.tsaha.nucleus.data.datasource.remote.api.PlanetApiImpl
import com.tsaha.nucleus.data.datasource.local.runtimememory.InMemoryPlanetDataSource
import com.tsaha.nucleus.data.datasource.local.runtimememory.PlanetLocalDataSource
import com.tsaha.nucleus.data.datasource.remote.PlanetRemoteDataSource
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
    singleOf(::InMemoryPlanetDataSource) { bind<PlanetLocalDataSource>() }
    singleOf(::PlanetRemoteDataSource)
    singleOf(::PlanetRepositoryImpl) { bind<PlanetRepository>() }
}