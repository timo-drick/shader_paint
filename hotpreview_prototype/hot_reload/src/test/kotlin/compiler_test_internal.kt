package test

import de.drick.compose.live_composable.KotlinLiveCompiler
import de.drick.compose.live_composable.deleteDir
import de.drick.compose.live_composable.kotlinFileClassName
import de.drick.compose.live_composable.loadClass
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import kotlin.test.Test
import kotlin.time.measureTime


class EmbeddedCompilerTest {

    @Test
    fun testCompileWithInternal() {
        println("Hello test")
        val rootDir = File("src/main/kotlin/test")
        val fileInternalFunction = File(rootDir, "internal_test_function.kt")
        val fileUsesInternalFunction = File(rootDir, "test_function_uses_internal.kt")
        val fileNewFile = File(rootDir, "new_file_test.kt")
        val destinationFolder = File("runtime")
        val classFolder = File(destinationFolder, "class")
        val allFilesList = listOf(fileInternalFunction, fileUsesInternalFunction)
        deleteDir(destinationFolder)
        destinationFolder.mkdirs()

        val compiler = KotlinLiveCompiler(destinationFolder)
        runBlocking {
            compiler.initialisation()
            compiler.compile(allFilesList, emptyList())
            compiler.compile(listOf(fileInternalFunction), emptyList())
            compiler.compile(listOf(fileInternalFunction), emptyList())
            compiler.compile(listOf(fileUsesInternalFunction), emptyList())
            compiler.compile(listOf(fileUsesInternalFunction), emptyList())
            val className = kotlinFileClassName(fileUsesInternalFunction)
            println("Loading class: $className")
            val loadedClass = loadClass(classFolder, className)
            println("Loaded: $loadedClass")

            compiler.compile(listOf(fileNewFile), emptyList())
            loadClass(classFolder, kotlinFileClassName(fileNewFile))
        }
    }
}
