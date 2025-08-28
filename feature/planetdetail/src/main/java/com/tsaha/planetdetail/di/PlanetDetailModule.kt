package com.tsaha.planetdetail.di

import com.tsaha.planetdetail.PlanetDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val planetDetailModule = module {
    viewModel { PlanetDetailViewModel(planetRepository = get()) }
}
