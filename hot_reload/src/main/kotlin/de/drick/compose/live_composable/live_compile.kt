package de.drick.compose.live_composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.reflect.asComposableMethod
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.drick.compose.hot_preview.RenderedImage
import de.drick.compose.hot_preview.analyzeClass
import de.drick.compose.hot_preview.renderMethod
import de.drick.compose.hotpreview.HotPreview
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val messageCollector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, false)

data class HotPreviewFunction(
    val name: String,
    val annotation: List<HotPreview>
)

data class HotPreviewFile(
    val name: String,
    val className: String,
    val fileList: List<File>, //Because maybe we do have several source files. Currently we can not get the exact source file.
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
    hotPreviewFile: HotPreviewFile,
    cfgRuntimeFolder: String,
    cfgJvmTarget: String = "17"
): HotPreviewState {
    val hotPreviewState = remember {
        HotPreviewState()
    }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val compileCounter = hotReloadCompile(
        fileList = hotPreviewFile.fileList,
        cfgJvmTarget = cfgJvmTarget,
        cfgRuntimeFolder = cfgRuntimeFolder
    )
    LaunchedEffect(compileCounter) {
        withContext(Dispatchers.Default) {
            try {
                val (newClass, loadingTime) = measureTimedValue {
                    loadClass(File(cfgRuntimeFolder, "class"), hotPreviewFile.className)
                }
                println("Class loading time: $loadingTime")
                val (functionList, analyzeTime) = measureTimedValue {
                    analyzeClass(newClass)
                }
                println("Class analyzing time: $analyzeTime")
                val (previewList, renderTime) = measureTimedValue {
                    functionList.map { function ->
                        println("F: $function")
                        val method = newClass.methods.find { it.name == function.name }?.asComposableMethod()
                        val images = function.annotation.map { annotation ->
                            val widthDp = annotation.widthDp.dp
                            val heightDp = annotation.heightDp.dp
                            method?.let {
                                renderMethod(
                                    method = it,
                                    size = DpSize(widthDp, heightDp),
                                    density = Density(2f, annotation.fontScale),
                                    isDarkTheme = annotation.darkMode
                                )
                            }
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

@Composable
fun hotReloadCompile(
    fileList: List<File>,
    cfgJvmTarget: String,
    cfgRuntimeFolder: String
): Int {
    var compileCounter by remember { mutableStateOf(0 ) }

    val compilerArgs = remember(fileList) {
        val classPath = System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .filter {
                File(it).exists() && File(it).canRead()
            }.joinToString(":")
        copyResourceFolder(cfgRuntimeFolder, "compilerPlugins")
        val pluginPathList = File("$cfgRuntimeFolder/compilerPlugins").listFiles().map { it.toString() }.toTypedArray()
        pluginPathList.forEach {
            println("Compiler plugin path: $it")
        }
        File(cfgRuntimeFolder).mkdir()
        K2JVMCompilerArguments().apply {
            freeArgs = fileList.map { it.path }
            destination = "$cfgRuntimeFolder/class"
            classpath = classPath
            pluginClasspaths = pluginPathList
            noJdk = false
            noReflect = true
            noStdlib = true
            jvmTarget = cfgJvmTarget
            script = false
            noOptimize = true
            noOptimizedCallableReferences = true
            reportPerf = false
        }
    }
    val compiler = remember {
        K2JVMCompiler()
    }
    val fileChangeCounter = fileMonitor(fileList)
    LaunchedEffect(fileChangeCounter, compilerArgs) {
        withContext(Dispatchers.Default) {
            try {
                val compileTime = measureTime {
                    compiler.exec(messageCollector, Services.EMPTY, compilerArgs)
                }
                println("Compile time: $compileTime")
                compileCounter++
            } catch (err: Throwable) {
                if (err is CancellationException) {
                    println("Compilation cancelled")
                    throw err
                }
                err.printStackTrace()
            }
        }
    }
    return compileCounter
}
