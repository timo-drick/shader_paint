package de.drick.compose.hot_preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.singleWindowApplication
import de.drick.compose.live_composable.HotPreviewFile
import de.drick.compose.live_composable.hotReloadPreview
import java.io.File


fun startHotPreview(
    srcDirList: List<String>,
    runtimeFolder: String
) {
    val srcFolderList = srcDirList.map { File(it) }
    val runtime = File(runtimeFolder)
    deleteDir(runtime)
    runtime.mkdirs()
    //Clean runtime folder

    println("Runtime folder for class compilation: ${runtime.path}")

    val kotlinFileList = collectKotlinFiles(srcFolderList)
    kotlinFileList.forEach {
        println(it)
    }
    val previewFiles = collectHotPreviewFiles(kotlinFileList)
    val initialPreviewList = previewFiles.map { file ->
        HotPreviewFile(
            name = file.name,
            className = kotlinFileClassNames(file),
            fileList = kotlinFileList
        )
    }
    singleWindowApplication(
        title = "Compose Preview"
    ) {
        val files = initialPreviewList//monitorAnnotations(fileList, initialPreviewList, classLoader)
        val hotPreviewFile = files.lastOrNull()
        if (hotPreviewFile != null) {
            val reloadState = hotReloadPreview(
                hotPreviewFile = hotPreviewFile,
                cfgRuntimeFolder = runtimeFolder
            )
            val instance = reloadState.hotReloadInstance
            val colorScheme = if (isSystemInDarkTheme())
                darkColorScheme()
            else
                lightColorScheme()
            MaterialTheme(
                colorScheme = colorScheme
            ) {
                Surface {
                    PreviewGridPanel(instance.preview)
                }
            }
        }
    }
}
