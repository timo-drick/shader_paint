package de.drick.compose.live_composable

import de.drick.compose.hotpreview.HotPreview
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.FqName
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

fun collectHotPreviewFiles(kotlinFileList: List<File>) = kotlinFileList.filter { file ->
    file.isHotPreviewFile()
}

private val annotationNameFq = checkNotNull(HotPreview::class.qualifiedName)
fun File.isHotPreviewFile() = useLines { lines ->
    // Check if anywhere in the file the annotation is used or imported
    lines.find { it.contains(annotationNameFq) } != null
}

private val packageMatcher = Regex("""package\s+([a-z][a-z0-9_]*(\.[a-z0-9_]+)*[a-z0-9_]*)""")
fun kotlinFileClassName(kotlinFile: File): String {
    //TODO support also the @JvmName annotation
    val packageName = kotlinFile.useLines { lines ->
        lines.find { it.trimStart().startsWith("package") }?.let { packageLine ->
            packageMatcher
                .find(packageLine)
                ?.groupValues
                ?.getOrNull(1)
        }
    } ?: ""
    val className = PackagePartClassUtils.getPackagePartFqName(FqName(packageName), kotlinFile.name)
        .toString()
    return className
}

