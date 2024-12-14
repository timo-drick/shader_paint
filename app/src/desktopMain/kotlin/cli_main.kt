package shaderpaint

import de.drick.compose.hot_preview.startHotPreview
import de.drick.compose.hotpreview.SourceSet

fun main() {
    val src = SourceSet(
        commonSrcDir = "src/commonMain/kotlin",
        desktopSrcDir = "src/desktopMain/kotlin"
    )
    val runtimeFolder = "runtime"
    startHotPreview(listOf(src), runtimeFolder)
}
