package shaderpaint

import de.drick.compose.hot_preview.startHotPreview

fun main() {
    val srcList = listOf(
        "/home/timo/projects/compose/shader_pain_t/app/src/commonMain/kotlin"
    )
    val runtimeFolder = "runtime"
    startHotPreview(srcList, runtimeFolder)
}
