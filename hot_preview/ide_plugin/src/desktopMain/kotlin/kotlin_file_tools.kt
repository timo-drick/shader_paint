package de.drick.compose.hot_preview

import de.drick.compose.hotpreview.HotPreview
import java.io.File

fun deleteDir(file: File) {
    file.listFiles()?.let { contents ->
        contents.forEach {
            deleteDir(it)
        }
    }
    file.delete()
}

fun collectKotlinFiles(srcDirList: List<File>) =
    srcDirList.filter { it.exists() }.flatMap { folder ->
        folder.walkTopDown().filter { it.isFile && it.extension == "kt"}
    }

private val annotationNameFq = checkNotNull(HotPreview::class.qualifiedName)
fun collectHotPreviewFiles(kotlinFileList: List<File>) = kotlinFileList.filter { file ->
    file.useLines { lines ->
        // Check if anywhere in the file the annotation is used or imported
        lines.find { it.contains(annotationNameFq) } != null
    }
}

private val packageMatcher = Regex("""package\s+([a-z][a-z0-9_]*(\.[a-z0-9_]+)*[a-z0-9_]*)""")
fun kotlinFileClassNames(kotlinFile: File): String {
    val packageName = kotlinFile.useLines { lines ->
        lines.find { it.trimStart().startsWith("package") }?.let { packageLine ->
            packageMatcher
                .find(packageLine)
                ?.groupValues
                ?.getOrNull(1)
        }
    }
    val name = kotlinFile.nameWithoutExtension.replaceFirstChar { it.uppercase() }
    val ext = kotlinFile.extension.replaceFirstChar { it.uppercase() }
    val className = "$name$ext"
    return if (packageName != null) "$packageName.$className" else className
}

