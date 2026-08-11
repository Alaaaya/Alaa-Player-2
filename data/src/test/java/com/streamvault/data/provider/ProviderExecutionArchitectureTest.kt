package com.streamvault.data.provider

import com.google.common.truth.Truth.assertThat
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isDirectory
import kotlin.streams.toList
import org.junit.Test

class ProviderExecutionArchitectureTest {
    private val repositoryRoot: Path by lazy {
        generateSequence(Paths.get(System.getProperty("user.dir")).toAbsolutePath()) { it.parent }
            .first { Files.exists(it.resolve("settings.gradle.kts")) }
    }

    @Test
    fun `protocol clients are constructed only by the typed client factory`() {
        val allowed = setOf(
            "data/src/main/java/com/streamvault/data/provider/TypedProviderClientFactory.kt",
            "data/src/main/java/com/streamvault/data/remote/stalker/StalkerProvider.kt",
            "data/src/main/java/com/streamvault/data/remote/xtream/XtreamProvider.kt"
        )
        val constructorPattern = Regex("\\b(?:StalkerProvider|XtreamProvider)\\s*\\(")
        val violations = productionKotlinFiles()
            .filter { path ->
                constructorPattern.containsMatchIn(path.readText()) &&
                    path.repoRelativePath() !in allowed
            }
            .map { it.repoRelativePath() }

        assertThat(violations).isEmpty()
    }

    @Test
    fun `runtime resolver boundaries do not branch on provider type`() {
        val boundaries = listOf(
            "data/src/main/java/com/streamvault/data/remote/xtream/XtreamStreamUrlResolver.kt",
            "data/src/main/java/com/streamvault/data/manager/RecordingManagerImpl.kt",
            "data/src/main/java/com/streamvault/data/epg/EpgResolutionEngine.kt"
        )
        val violations = boundaries.filter { relative ->
            repositoryRoot.resolve(relative).readText().contains("ProviderType.")
        }

        assertThat(violations).isEmpty()
    }

    @Test
    fun `sync execution does not redispatch on provider type after registry resolution`() {
        val source = repositoryRoot.resolve(
            "data/src/main/java/com/streamvault/data/sync/SyncManager.kt"
        ).readText()
        val providerTypeDispatch = Regex(
            """when\s*\(\s*(?:provider|providerEntity)\.type\s*\)"""
        )

        assertThat(providerTypeDispatch.containsMatchIn(source)).isFalse()
        assertThat(source).contains("providerSyncRegistry.resolve(snapshot)")
        assertThat(source).contains("syncAdapter.syncFull(")
        assertThat(source).contains("syncAdapter.syncSection(")
        assertThat(source).contains("syncAdapter.syncGuide(")
    }

    @Test
    fun `legacy provider interface cannot return`() {
        val violations = productionKotlinFiles()
            .filter { path -> path.readText().contains("IptvProvider") }
            .map { it.repoRelativePath() }

        assertThat(violations).isEmpty()
    }

    private fun productionKotlinFiles(): List<Path> =
        listOf("domain/src/main", "data/src/main", "app/src/main")
            .flatMap { relative ->
                Files.walk(repositoryRoot.resolve(relative)).use { paths ->
                    paths.filter { path -> !path.isDirectory() && path.extension == "kt" }.toList()
                }
            }

    private fun Path.repoRelativePath(): String =
        repositoryRoot.relativize(this).invariantSeparatorsPathString

    private fun Path.readText(): String = String(Files.readAllBytes(this), Charsets.UTF_8)
}
