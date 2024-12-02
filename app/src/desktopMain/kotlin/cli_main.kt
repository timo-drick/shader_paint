package shaderpaint

import de.drick.compose.hot_preview.startHotPreview

fun main() {
    val srcList = listOf(
        "src/commonMain/kotlin"
    )
    val runtimeFolder = "runtime"
    startHotPreview(srcList, runtimeFolder)
}
