package test

import de.drick.compose.live_composable.KotlinLiveCompiler
import de.drick.compose.live_composable.kotlinFileClassName
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import kotlin.time.measureTime

fun main() {
    println("Home: ${File(".").canonicalPath}")
    val rootDir = File("preview_system/hotpreview_prototype/hot_reload/src/main/kotlin/test/")
    val fileInternalFunction = File(rootDir, "internal_test_function.kt")
    val fileUsesInternalFunction = File(rootDir, "test_function_uses_internal.kt")
    val destinationFolder = File("test")
    val allFilesList = listOf(fileInternalFunction, fileUsesInternalFunction)
    val compiler = KotlinCompiler(destinationFolder)
    val friendsPathList = arrayOf("test/class")
    val className = kotlinFileClassName(fileUsesInternalFunction)
    println("File: ${fileUsesInternalFunction.name} -> Class: $className")
    runBlocking {
        //compiler.initialisation()
        compiler.compile(
            fileList = listOf(fileInternalFunction, fileUsesInternalFunction),
            //commonSrcFileList = allFilesList
        )
        compiler.compile(
            fileList = listOf(fileUsesInternalFunction),
            //commonSrcFileList = listOf()
        )
        compiler.compile(
            fileList = listOf(fileUsesInternalFunction),
            //commonSrcFileList = listOf()
        )
    }
}


class KotlinCompiler(
    private val destinationFolder: File
) {
    private val targetJvmVersion = System.getProperty("java.vm.specification.version")
    private val systemClassPath = System.getProperty("java.class.path")
        .split(File.pathSeparator)
        .filter {
            File(it).exists() && File(it).canRead()
        }
        .filter { it.endsWith(".jar") }
    private val classPath = (systemClassPath + destinationFolder.canonicalPath).joinToString(":")
    private val compiler = K2JVMCompiler()

    fun compile(
        fileList: List<File>
    ) {
        val compilerArgs = K2JVMCompilerArguments().apply {
            freeArgs = fileList.map { it.path }
            //friendPaths = arrayOf(destinationFolder.path)
            destination = destinationFolder.path
            classpath = classPath
            noJdk = false
            noReflect = true
            noStdlib = true
            jvmTarget = targetJvmVersion
            script = false
            noOptimize = true
            reportPerf = false
            incrementalCompilation = false
        }

        val compileTime = measureTime {
            compiler.exec(messageCollector, Services.EMPTY, compilerArgs)
        }
        println("Compile time: $compileTime")
    }
}

private val messageCollector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, false)
