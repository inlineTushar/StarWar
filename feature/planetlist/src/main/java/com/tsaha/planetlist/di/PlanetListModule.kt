package com.tsaha.planetlist.di

import com.tsaha.planetlist.PlanetListUiUseCase
import com.tsaha.planetlist.PlanetListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val planetListModule = module {
    viewModel { PlanetListViewModel(planetListUseCase = get()) }
    single { PlanetListUiUseCase(planetRepository = get()) }
}
