package de.drick.compose.live_composable

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService
import kotlin.reflect.KClass

fun loadClass(path: File, className: String): Class<*> {
    val parent = Class.forName(className).classLoader
    val classLoader = CompiledScriptClassLoader(
        parent = parent,
        path = path.absoluteFile
    )
    return classLoader.loadClass(className)
}

/**
 * We can not return the class itself because after reloading it is another type.
 * So only an interface can be returned that is loaded in the parent class loader.
 */
@Suppress("UNCHECKED_CAST")
fun <T: I, I: Any>loadClass(path: File, clazz: KClass<T>): I {
    val className = checkNotNull(clazz.qualifiedName)
    val classLoader = CompiledScriptClassLoader(
        parent = clazz.java.classLoader,
        path = path.absoluteFile
    )
    val reloadedClazz = classLoader.loadClass(className).kotlin
    return reloadedClazz.constructors.first().call() as I
}

class CompiledScriptClassLoader(parent: ClassLoader, private val path: File) : ClassLoader(parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> = if (class2File(name).exists()) {
        val clazzFile = class2File(name)
        println("Load class: $name")
        val bytes = clazzFile.readBytes()
        defineClass(name, bytes, 0, bytes.size)
    } else {
        //println("Class not found: $name")
        parent.loadClass(name)
    }
    private fun class2File(className: String): File {
        val classFileName = className.replace(".", "/") + ".class"
        return File(path, classFileName)
    }
}

private fun registerRecursive(service: WatchService, dir: File) {
    dir.toPath().register(service, StandardWatchEventKinds.ENTRY_MODIFY)
    println("Register: ${dir.absolutePath}")
    dir.listFiles()
        ?.filter { it.isDirectory }
        ?.forEach { registerRecursive(service, it) }
}

@Composable
fun directoryMonitor(directory: File): Int {
    var counter by remember(directory) { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    DisposableEffect(directory) {
        val watchService = FileSystems.getDefault().newWatchService()
        scope.launch(Dispatchers.IO) {
            registerRecursive(watchService, directory)
            while (isActive) {
                try {
                    val watchKey = watchService.take()
                    println("Event: ${watchKey.pollEvents().joinToString { "${it.context()}" }}")
                    watchKey.reset()
                    counter++
                } catch (err: java.nio.file.ClosedWatchServiceException) {
                    println("Closed watch service")
                    break
                }
            }
        }
        onDispose {
            watchService.close()
        }
    }
    return counter
}

@Composable
fun fileMonitor(fileList: List<File>): Int {
    var counter by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    DisposableEffect(fileList) {
        val watchService = FileSystems.getDefault().newWatchService()
        scope.launch(Dispatchers.IO) {
            fileList.map { it.parentFile.toPath() }.forEach {
                it.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
            }
            val fileNameList = fileList.map { it.name }
            var lastChange: Long = 0
            while (isActive) {
                try {
                    var fileChanged = false
                    val watchKey = watchService.take()
                    val dirPath = watchKey.watchable() as? Path ?: break
                    for (event in watchKey.pollEvents()) {
                        val changedFile = dirPath.resolve(event.context() as Path)
                        //if (fileNameList.contains(changedFile)) fileChanged = true
                        fileChanged = true
                        println("Changed file: $changedFile event: ${event.kind()} context: ${event.context()} ${event.count()}")
                    }
                    val cTime = System.currentTimeMillis()
                    if (fileChanged && cTime - 50 > lastChange) {
                        println("File changed: $cTime - 50 > $lastChange")
                        lastChange = cTime
                        counter++
                    }
                    if (!watchKey.reset()) {
                        watchKey.cancel()
                        watchService.close()
                        break
                    }
                } catch (err: java.nio.file.ClosedWatchServiceException) {
                    println("Closed watch service")
                    break
                }
            }
        }
        onDispose {
            watchService.close()
        }
    }
    return counter
}
