package com.streamvault.app

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseStartupGateTest {
    @Test
    fun `successful open transitions to ready`() = runTest {
        val gate = DatabaseStartupGate { }

        gate.open()

        assertThat(gate.state.value).isEqualTo(DatabaseStartupState.Ready)
    }

    @Test
    fun `database open is deferred until an explicit startup request`() = runTest {
        var attempts = 0
        val gate = DatabaseStartupGate { attempts++ }

        assertThat(attempts).isEqualTo(0)

        gate.start(this)
        advanceUntilIdle()

        assertThat(attempts).isEqualTo(1)
        assertThat(gate.state.value).isEqualTo(DatabaseStartupState.Ready)
    }

    @Test
    fun `failed open is contained and retry can recover`() = runTest {
        var attempts = 0
        val gate = DatabaseStartupGate {
            attempts++
            if (attempts == 1) error("jdbc:sqlite://secret-user:secret-password@host/database")
        }

        gate.open()
        val failure = gate.state.value as DatabaseStartupState.Failed
        assertThat(failure.errorType).isEqualTo("IllegalStateException")
        assertThat(failure.userMessage).doesNotContain("secret")

        gate.open()
        assertThat(gate.state.value).isEqualTo(DatabaseStartupState.Ready)
        assertThat(attempts).isEqualTo(2)
    }

    @Test
    fun `concurrent calls perform one database open`() = runTest {
        var attempts = 0
        val gate = DatabaseStartupGate { attempts++ }

        val calls = List(10) { async { gate.open() } }
        calls.forEach { it.await() }

        assertThat(attempts).isEqualTo(1)
    }

    @Test
    fun `catalog readers stay behind gate until secure policy restoration completes`() = runTest {
        val policyRestored = kotlinx.coroutines.CompletableDeferred<Unit>()
        val gate = DatabaseStartupGate { policyRestored.await() }
        val opening = async { gate.open() }
        advanceUntilIdle()
        assertThat(gate.state.value).isEqualTo(DatabaseStartupState.Opening)
        policyRestored.complete(Unit)
        opening.await()
        assertThat(gate.state.value).isEqualTo(DatabaseStartupState.Ready)
    }

    @Test
    fun `secure policy restore failure cannot publish cached catalog readiness`() = runTest {
        val gate = DatabaseStartupGate { error("Secure policy unavailable") }
        gate.open()
        assertThat(gate.state.value).isInstanceOf(DatabaseStartupState.Failed::class.java)
    }

    @Test
    fun `one startup task failure does not cancel sibling tasks`() = runTest {
        val completed = mutableListOf<String>()
        val failures = mutableListOf<String>()

        runContainedStartupTasks(
            tasks = listOf(
                StartupTask("broken") { error("boom") },
                StartupTask("healthy") { completed += "healthy" }
            ),
            onFailure = { name, _ -> failures += name }
        )

        assertThat(completed).containsExactly("healthy")
        assertThat(failures).containsExactly("broken")
    }
}
