package com.streamvault.app.ui

import com.google.common.truth.Truth.assertThat
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Test

class CloseConfirmationLocaleParityTest {
    private val keys = listOf(
        "player_close_playback_title",
        "player_close_playback_desc",
        "player_close_playback_confirm",
        "player_close_playback_cancel",
        "settings_confirm_close_playback",
        "settings_confirm_close_playback_subtitle",
        "app_close_confirm_title",
        "app_close_confirm_desc"
    )

    private fun resDir(): File =
        listOf(File("src/main/res"), File("app/src/main/res")).first { it.isDirectory }

    private fun strings(file: File): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length).associate {
            val node = nodes.item(it)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    @Test
    fun everyLocaleDefinesCloseConfirmationStrings() {
        val defaults = strings(File(resDir(), "values/strings.xml"))
        keys.forEach { assertThat(defaults[it]).isNotEmpty() }
        val localeFiles = resDir().listFiles { f -> f.isDirectory && f.name.startsWith("values-") }
            .orEmpty()
            .map { File(it, "strings.xml") }
            .filter { it.isFile }
        assertThat(localeFiles).isNotEmpty()
        localeFiles.forEach { file ->
            val values = strings(file)
            keys.forEach { key ->
                assertThat(values[key]).isNotEmpty()
            }
        }
    }

    @Test
    fun arabicTranslationsAreNotEnglishCopies() {
        val defaults = strings(File(resDir(), "values/strings.xml"))
        val arabic = strings(File(resDir(), "values-ar/strings.xml"))
        keys.forEach { assertThat(arabic[it]).isNotEqualTo(defaults[it]) }
    }

    @Test
    fun placeholdersMatchDefaultLocale() {
        val defaults = strings(File(resDir(), "values/strings.xml"))
        val regex = Regex("%\\d\\$[sd]")
        resDir().listFiles { f -> f.isDirectory && f.name.startsWith("values-") }.orEmpty()
            .map { File(it, "strings.xml") }.filter { it.isFile }.forEach { file ->
                val values = strings(file)
                keys.forEach { key ->
                    assertThat(regex.findAll(values.getValue(key)).map { it.value }.toSet())
                        .isEqualTo(regex.findAll(defaults.getValue(key)).map { it.value }.toSet())
                }
            }
    }
}
