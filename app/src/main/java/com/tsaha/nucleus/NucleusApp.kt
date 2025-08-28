package com.tsaha.nucleus

import android.app.Application
import com.tsaha.nucleus.core.di.coreModule
import com.tsaha.nucleus.data.di.dataModule
import com.tsaha.planetlist.di.planetListModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class NucleusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NucleusApp)
            modules(
                coreModule,
                dataModule,      // dataModule includes httpModule internally
                planetListModule // PlanetList feature module
            )
        }
    }
}
