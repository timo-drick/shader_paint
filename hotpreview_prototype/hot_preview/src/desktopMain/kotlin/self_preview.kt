package de.drick.compose.hot_preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.live_composable.HotPreviewFile
import java.io.File

private fun createHotPreviewFile(index: Int) = HotPreviewFile(
    file = File("File$index.kt"),
    className = "",
    hotPreviewFunctions = emptyList()
)

@HotPreview
@Composable
fun PreviewHotPreviewTest() {
    val hotPreviewList: List<HotPreviewFile> = listOf(
        createHotPreviewFile(1),
        createHotPreviewFile(2),
        createHotPreviewFile(3)
    )

    var selectedTabIndex by remember { mutableStateOf(1) }
    MaterialTheme {
        Surface(Modifier.size(800.dp)) {
            Column(Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    hotPreviewList.forEachIndexed { index, hotPreviewData ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(hotPreviewData.file.name)
                            }
                        )
                    }
                }
                Text("Hello HotPreview2")
            }
        }
    }
}