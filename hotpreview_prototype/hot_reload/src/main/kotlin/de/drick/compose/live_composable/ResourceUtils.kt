package de.drick.compose.live_composable

import java.io.File
import java.net.URL
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


fun copyResourceFolder(targetBaseFolder: File, folderName: String) {
    val uri = ClassLoader.getSystemResource(folderName).toURI()
    val fileSystem: FileSystem?
    val isJar = uri.scheme == "jar"
    fileSystem = if (isJar) {
        FileSystems.newFileSystem(uri, Collections.emptyMap<String, Any>())
    } else {
        null
    }
    val folder = File(targetBaseFolder, folderName)
    folder.mkdirs()
    Files.newDirectoryStream(Paths.get(uri)).use { directoryStream ->
        for (path in directoryStream) {
            println("Path: $path")
            val pathName = if (isJar) path.subpath(0, path.count()).toString() else folderName + "/" + path.fileName
            val stream = ClassLoader.getSystemResourceAsStream(pathName)
            val fileName = path.fileName.toString()
            println("Check $fileName")
            val dbFile = File(folder, fileName)
            if(!dbFile.exists()) {
                println("Copy ${dbFile.toPath()}")
                Files.copy(stream, dbFile.toPath())
            }

        }
    }
    fileSystem?.close()
}

fun readFile(file: String): String {
    val uri = ClassLoader.getSystemResource(file).toURI()
    val fileSystem: FileSystem? = if (uri.scheme == "jar") {
        FileSystems.newFileSystem(uri, Collections.emptyMap<String, Any>())
    } else {
        null
    }
    val text = ClassLoader.getSystemResource(file).readText()
    fileSystem?.close()

    return text
}

fun useRessource(file: String, block: (URL) -> Unit ) {
    val uri = ClassLoader.getSystemResource(file).toURI()
    val fileSystem: FileSystem? = if (uri.scheme == "jar") {
        FileSystems.newFileSystem(uri, Collections.emptyMap<String, Any>())
    } else {
        null
    }
    val url = ClassLoader.getSystemResource(file)
    block(url)
    fileSystem?.close()
}