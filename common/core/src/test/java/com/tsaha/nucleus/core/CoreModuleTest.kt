package com.tsaha.nucleus.core

import com.tsaha.nucleus.core.di.coreModule
import com.tsaha.nucleus.core.network.HttpClient
import org.junit.Test
import org.junit.Assert.*

/**
 * Test class for Core module functionality
 */
class CoreModuleTest {

    @Test
    fun `test core module configuration`() {
        // Test that the Koin module is properly configured
        org.koin.test.check.checkModules {
            modules(coreModule)
        }
    }

    @Test
    fun `test network client creation`() {
        // Test that NetworkClient can be created without errors
        // Logging level is automatically determined by BuildConfig.DEBUG
        val client = HttpClient.create()
        assertNotNull(client)
        client.close()
    }
}