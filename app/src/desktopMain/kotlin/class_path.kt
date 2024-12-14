package shaderpaint

import java.io.File

fun main() {
    val classPath = System.getProperty("java.class.path")
        .split(File.pathSeparator)
        .filter {
            File(it).exists() && File(it).canRead()
        }.joinToString(",\n") { "\"$it\"" }
    println(classPath)
}