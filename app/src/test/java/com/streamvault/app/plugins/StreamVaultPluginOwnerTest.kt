package com.streamvault.app.plugins

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StreamVaultPluginOwnerTest {

    @Test
    fun `plugin owner remains distinct when packages reuse a manifest ID`() {
        val first = StreamVaultPluginOwner("com.example.first", "FirstService", "shared-id")
        val second = StreamVaultPluginOwner("com.example.second", "SecondService", "shared-id")

        assertThat(first).isNotEqualTo(second)
    }

    @Test
    fun `plugin owner remains distinct for two services in one package`() {
        val first = StreamVaultPluginOwner("com.example.plugin", "FirstService", "shared-id")
        val second = StreamVaultPluginOwner("com.example.plugin", "SecondService", "shared-id")

        assertThat(first).isNotEqualTo(second)
    }
}
