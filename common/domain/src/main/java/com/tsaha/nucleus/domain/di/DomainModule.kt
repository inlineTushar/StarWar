package com.tsaha.nucleus.domain.di

import com.tsaha.nucleus.domain.PlanetListUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin module for domain layer dependencies.
 * Provides use cases that orchestrate business logic between data and presentation layers.
 */
val domainModule = module {
    singleOf(::PlanetListUseCase)
}
