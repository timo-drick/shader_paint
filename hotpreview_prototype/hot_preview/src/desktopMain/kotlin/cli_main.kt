package de.drick.compose.hot_preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.ui.window.singleWindowApplication
import de.drick.compose.hotpreview.SourceSet
import de.drick.compose.live_composable.directoryMonitorFlow
import de.drick.compose.live_composable.filterKotlinFiles
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() {
    val sourceSet = SourceSet(
        commonSrcDir = "src/commonMain/kotlin",
        desktopSrcDir = "src/desktopMain/kotlin"
    )
    startHotPreview(
        sourceSets = listOf(sourceSet),
        runtimeFolder = "runtime"
    )
}

fun testFileMonitor() {
    runBlocking {
        val dir = File("src/commonMain/kotlin")
        directoryMonitorFlow(dir).filterKotlinFiles().collect {
            println(it)
        }
    }
}

fun startHotPreview(
    sourceSets: List<SourceSet>,
    runtimeFolder: String
) {
    val runtime = File(runtimeFolder)

    println("Runtime folder for class compilation: ${runtime.path}")

    singleWindowApplication(
        title = "Compose Preview"
    ) {
        val colorScheme = if (isSystemInDarkTheme())
            darkColorScheme()
        else
            lightColorScheme()
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            PreviewMainScreen(
                sourceSets = sourceSets,
                runtimeFolder = runtimeFolder
            )
        }
    }
}
