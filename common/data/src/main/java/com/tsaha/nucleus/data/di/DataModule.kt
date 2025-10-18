package com.tsaha.nucleus.data.di

import com.tsaha.nucleus.data.datasource.local.runtimememory.InMemoryPlanetDataSource
import com.tsaha.nucleus.data.datasource.local.runtimememory.PlanetLocalDataSource
import com.tsaha.nucleus.data.datasource.remote.api.PlanetApi
import com.tsaha.nucleus.data.datasource.remote.api.PlanetApiImpl
import com.tsaha.nucleus.data.repository.PlanetRepository
import com.tsaha.nucleus.data.repository.PlanetRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Data layer dependency injection module.
 *
 * Provides:
 * - PlanetApi → PlanetApiImpl
 * - PlanetLocalDataSource → InMemoryPlanetDataSource
 * - PlanetRepository → PlanetRepositoryImpl
 * - PlanetRemoteDataSource (automatically via @Inject constructor)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindPlanetApi(impl: PlanetApiImpl): PlanetApi

    @Binds
    @Singleton
    abstract fun bindPlanetLocalDataSource(impl: InMemoryPlanetDataSource): PlanetLocalDataSource

    @Binds
    @Singleton
    abstract fun bindPlanetRepository(impl: PlanetRepositoryImpl): PlanetRepository
}