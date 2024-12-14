package de.drick.compose.live_composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.reflect.ComposableMethod
import androidx.compose.runtime.reflect.asComposableMethod
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.drick.compose.hot_preview.RenderedImage
import de.drick.compose.hot_preview.analyzeClass
import de.drick.compose.hot_preview.renderMethod
import de.drick.compose.hotpreview.HotPreview
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.measureTimedValue

data class HotPreviewFunction(
    val name: String,
    val annotation: List<HotPreview>,
    val composableMethod: ComposableMethod
)

data class HotPreviewFile(
    val file: File,
    val className: String,
    val hotPreviewFunctions: List<HotPreviewFunction>
)

data class HotPreviewData(
    val function: HotPreviewFunction,
    val image: List<RenderedImage?>,
)

data class HotReloadInstance(
    val preview: List<HotPreviewData>,
)

class HotPreviewState {
    var compiling by mutableStateOf(false)
    var hotReloadInstance by mutableStateOf(HotReloadInstance(persistentListOf()))
}

@Composable
fun hotReloadPreview(
    compileCounter: Int,
    hotPreviewFile: HotPreviewFile,
    cfgRuntimeFolder: String
): HotPreviewState {
    val hotPreviewState = remember(hotPreviewFile) {
        HotPreviewState()
    }

    LaunchedEffect(hotPreviewFile, compileCounter) {
        withContext(Dispatchers.Default) {
            try {
                val functionList = hotPreviewFile.hotPreviewFunctions
                val (previewList, renderTime) = measureTimedValue {
                    functionList.map { function ->
                        println("F: $function")
                        val images = function.annotation.map { annotation ->
                            val widthDp = annotation.widthDp.dp
                            val heightDp = annotation.heightDp.dp
                            renderMethod(
                                method = function.composableMethod,
                                size = DpSize(widthDp, heightDp),
                                density = Density(2f, annotation.fontScale),
                                isDarkTheme = annotation.darkMode
                            )
                        }
                        HotPreviewData(
                            function = function,
                            image = images
                        )
                    }
                }
                println("Render time: $renderTime")
                hotPreviewState.hotReloadInstance = HotReloadInstance(previewList)
                //liveMethod = HotReloadInstance(composableMethodList)
                //val previewMethod = newClass.declaredComposableMethod
            } catch (err: Throwable) {
                if (err is CancellationException) {
                    println("Compilation cancelled")
                    throw err
                }
                err.printStackTrace()
            }
        }
        hotPreviewState.compiling = false
    }
    // This method of hot reload swapping is working well as far as I tested it but the remembered variables get lost.
    return hotPreviewState
}
