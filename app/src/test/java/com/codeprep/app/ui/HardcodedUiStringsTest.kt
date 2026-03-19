package com.codeprep.app.ui

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedUiStringsTest {

    @Test
    fun userVisibleStrings_areBackedByResources() {
        val sourceRoot = locateSourceRoot()
        val uiRoot = File(sourceRoot, "ui")
        val relativeBase = sourceRoot.parentFile ?: sourceRoot
        val filesToScan = uiRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toMutableList()
            .apply {
                add(File(sourceRoot, "MainActivity.kt"))
                add(File(uiRoot, "auth/AuthViewModel.kt"))
                add(File(uiRoot, "friends/FriendsViewModels.kt"))
                add(File(uiRoot, "home/HomeViewModel.kt"))
            }
            .distinctBy { it.absolutePath }

        val violations = filesToScan.flatMap { file ->
            val content = file.readText()
            forbiddenPatterns.flatMap { pattern ->
                pattern.findAll(content).map { match ->
                    val lineNumber = content.substring(0, match.range.first).count { it == '\n' } + 1
                    "${file.relativeTo(relativeBase).path}:$lineNumber:${match.value.trim()}"
                }.toList()
            }
        }

        assertTrue(
            "Found hardcoded user-visible strings:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }

    private fun locateSourceRoot(): File {
        val candidates = listOf(
            File("src/main/java/com/codeprep/app"),
            File("app/src/main/java/com/codeprep/app")
        )
        return candidates.firstOrNull(File::exists)
            ?: error("Could not locate app source root from ${File(".").absolutePath}")
    }

    private companion object {
        val forbiddenPatterns = listOf(
            Regex("""\bText\(\s*"[^"\n]*[A-Za-z][^"\n]*"\s*[\),]"""),
            Regex("""\btext\s*=\s*"[^"\n]*[A-Za-z][^"\n]*""""),
            Regex("""\bcontentDescription\s*=\s*"[^"\n]*[A-Za-z][^"\n]*""""),
            Regex("""\btitle\s*=\s*\{\s*Text\(\s*"[^"\n]*[A-Za-z][^"\n]*"\s*\)"""),
            Regex("""\bplaceholder\s*=\s*\{\s*Text\(\s*"[^"\n]*[A-Za-z][^"\n]*"\s*\)"""),
            Regex("""\blabel\s*=\s*\{\s*Text\(\s*"[^"\n]*[A-Za-z][^"\n]*"\s*\)"""),
            Regex("""BottomNavItem\(\s*"[^"\n]*[A-Za-z][^"\n]*"\s*,"""),
            Regex("""AvatarPresetUi\(\s*"[^"\n]+",\s*"[^"\n]*[A-Za-z][^"\n]*"\s*,"""),
            Regex("""AuthState\.Error\(\s*it\.message\s*\?:\s*"[^"\n]*[A-Za-z][^"\n]*""""),
            Regex("""errorMessage(?:\.value)?\s*=\s*it\.message\s*\?:\s*"[^"\n]*[A-Za-z][^"\n]*""""),
            Regex("""errorMessage\.value\s*=\s*"[^"\n]*[A-Za-z][^"\n]*""""),
            Regex("""\?:\s*"User""""),
            Regex("""\?:\s*"Dev""""),
            Regex("""\?:\s*"Code"""")
        )
    }
}
