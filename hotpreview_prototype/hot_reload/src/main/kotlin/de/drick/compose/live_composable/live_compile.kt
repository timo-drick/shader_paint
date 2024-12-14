package de.drick.compose.live_composable

import androidx.compose.runtime.*
import de.drick.compose.hot_preview.analyzeClass
import de.drick.compose.hotpreview.SourceSet
import kotlinx.coroutines.*
import java.io.File
import java.net.URLClassLoader
import kotlin.time.measureTime

class ReloadCompileState {
    var compileCounter by mutableStateOf(0)
    var previewFileList by mutableStateOf<List<HotPreviewFile>>(emptyList())
}

@Composable
fun hotReloadCompile(
    sourceSets: List<SourceSet>,
    cfgRuntimeFolder: String
): ReloadCompileState {
    val state: ReloadCompileState = remember { ReloadCompileState() }

    LaunchedEffect(sourceSets, cfgRuntimeFolder) {
        val compiler = KotlinLiveCompiler(File(cfgRuntimeFolder))
        compiler.initialisation()
        val directories = sourceSets
            .flatMap {
                listOf(it.commonSrcDir, it.desktopSrcDir)
            }.filterNotNull()
            .map { File(it) }
        val kotlinFileChangeFlow = directoryMonitorFlow(directories).filterKotlinFiles()
        val previewFileMap = mutableMapOf<File, HotPreviewFile>()

        kotlinFileChangeFlow.collect { changedFileList ->
            println("File changes: $changedFileList")

            // TODO Find a reliable way to only recompile changed files

            // compile modules where files where changed

            sourceSets.forEachIndexed { index, sourceSet ->

                //Collect all files
                val desktopFileList = sourceSet.desktopSrcDir?.let { dir ->
                    File(dir).walkTopDown().filter { it.extension == "kt" }.toList()
                } ?: emptyList()

                val commonFileList = sourceSet.commonSrcDir?.let { dir ->
                    File(dir).walkTopDown().filter { it.extension == "kt" }.toList()
                } ?: emptyList()

                val changedSet = changedFileList.map { it.file }.toSet()
                if (changedSet.containsAny(desktopFileList) || changedSet.containsAny(commonFileList)) {
                    //Recompile
                    withContext(Dispatchers.Default) {
                        try {
                            println("Compiling started: $sourceSet")
                            val compileTime = measureTime {
                                compiler.compile(
                                    fileList = desktopFileList + commonFileList,//changedFileList.map { it.file },
                                    commonSrcFileList = commonFileList,
                                    //module = index.toString()
                                )
                            }
                            println("Compiling end duration: $compileTime")
                        } catch (err: Throwable) {
                            if (err is CancellationException) {
                                println("Compilation cancelled")
                                throw err
                            }
                            err.printStackTrace()
                        }
                    }
                }
            }

            //Remove deleted files
            changedFileList
                .filter { it.type == FileEvent.Type.DELETED }
                .forEach { fileEvent ->
                    previewFileMap.values.removeIf { it.file.canonicalPath == fileEvent.file.canonicalPath }
                }

            // Search for files with HotPreview annotations
            changedFileList
                .filter { it.type != FileEvent.Type.DELETED }
                .filter { it.file.isHotPreviewFile() }
                .forEach { fileEvent ->
                    val className = kotlinFileClassName(fileEvent.file)
                    //Load class and check for HotPreview annotations
                    val runtimeUrl = File(cfgRuntimeFolder, "class").toURI().toURL()
                    val classPath = System.getProperty("java.class.path")
                        .split(File.pathSeparator)
                        .map { File(it) }
                        .filter { it.exists() && it.canRead() }
                        .filter { it.extension == "jar" }
                        .map { it.toURI().toURL() }
                        .toTypedArray()
                    //val parent = Class.forName(className).classLoader
                    val parent = URLClassLoader(classPath)
                    //val cp = arrayOf(runtimeUrl) + classPath
                    val classLoader = CompiledScriptClassLoader(
                        parent = parent,
                        path = File(cfgRuntimeFolder, "class")
                    )
                    val clazz = classLoader.loadClass(className)
                    val functions = analyzeClass(clazz)
                    previewFileMap[fileEvent.file] = HotPreviewFile(
                        file = fileEvent.file,
                        className = className,
                        hotPreviewFunctions = functions
                    )
                }

            state.previewFileList = previewFileMap.values.filter {
                it.hotPreviewFunctions.isNotEmpty()
            }.toList()
            state.compileCounter++
        }
    }
    return state
}

fun <T> Collection<T>.containsAny(other: Collection<T>): Boolean {
    // Use HashSet instead of #toSet which uses a LinkedHashSet
    val set = if (this is Set) this else HashSet(this)
    for (item in other)
        if (set.contains(item)) // early return
            return true
    return false
}