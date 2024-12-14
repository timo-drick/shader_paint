package de.drick.compose.hot_preview

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.hotpreview.SourceSet
import de.drick.compose.live_composable.hotReloadCompile
import de.drick.compose.live_composable.hotReloadPreview


@Composable
fun PreviewMainScreen(
    sourceSets: List<SourceSet>,
    runtimeFolder: String = "runtime"
) {
    val state = hotReloadCompile(
        sourceSets = sourceSets,
        cfgRuntimeFolder = runtimeFolder
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (state.previewFileList.isEmpty()) {
        Text("No files with ${HotPreview::class.simpleName} annotation detected!")
    } else {
        val selectedPreviewFile by derivedStateOf {
            state.previewFileList.getOrElse(selectedTabIndex) {
                selectedTabIndex = state.previewFileList.size - 1
                state.previewFileList.last()
            }
        }

        val previewState = hotReloadPreview(
            compileCounter = state.compileCounter,
            hotPreviewFile = selectedPreviewFile,
            cfgRuntimeFolder = runtimeFolder
        )
        Surface {
            if (state.previewFileList.isNotEmpty()) {

                val instance = previewState.hotReloadInstance
                Column {
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        state.previewFileList.forEachIndexed { index, hotPreviewData ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(hotPreviewData.file.name)
                                }
                            )
                        }
                    }
                    PreviewGridPanel(instance.preview)
                }
            } else {
                Text("No files detected with HotPreview annotation!")
            }
        }
    }
}
