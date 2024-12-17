package de.drick.compose.hot_preview

import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.live_composable.HotPreviewFunction
import io.github.classgraph.ClassGraph
import io.github.classgraph.MethodInfo
import kotlin.reflect.KClass
import kotlin.reflect.jvm.kotlinFunction

data class ComposableFunctionInfo(
    val name: String,
    val sourceFileName: String,
    val className: String,
    val lineNumber: Int
)

fun analyzeClass(clazz: Class<*>): List<HotPreviewFunction> {
    return clazz.methods
        .mapNotNull { method ->
            method.kotlinFunction?.let { function ->
                val annotations = function.annotations
                    .filterIsInstance<HotPreview>()
                if (annotations.isEmpty())
                    null
                else HotPreviewFunction(
                    name = function.name,
                    annotation = annotations
                )
            }
        }
}

@Throws(Exception::class)
fun getComposableFunctionsAnnotatedWith(classLoader: ClassLoader, annotation: KClass<out Annotation>)
        : List<ComposableFunctionInfo> {
    val pkgName = annotation.java.`package`.name
    val annotationName = annotation.java.canonicalName
    val moduleInfo = ClassGraph().modulePathInfo
    println("Module info: $moduleInfo")
    val flist = ClassGraph()
        //.overrideClassLoaders(classLoader)
        .enableAllInfo()
        //.acceptPackages(pkgName) not working
        .scan()
        .use { scanResult ->
            scanResult.getClassesWithMethodAnnotation(annotationName).flatMap { routeClassInfo ->
                routeClassInfo.methodInfo
                    .filter { it.hasAnnotation(annotation.java) }
                    .map { method ->
                        val annotationInfo = method.annotationInfo.filter { it.name == annotationName }
                            .first()
                        //val fileName = annotationInfo.parameterValues.getValue("fileName") as String
                        //val file = File(fileName)
                        ComposableFunctionInfo(
                            name = method.name,
                            sourceFileName = method.classInfo.sourceFile,
                            className = method.className,
                            lineNumber = method.minLineNum
                        )
                    }
            }
        }
    return flist
    // if parameter needed:
    // method.getAnnotationInfo(routeAnnotation).parameterValues.map { it.value }
}

@Throws(Exception::class)
fun getMethodsAnnotatedWith(classLoader: ClassLoader, annotation: KClass<out Annotation>)
: List<MethodInfo> {
    val pkgName = annotation.java.`package`.name
    val annotationName = annotation.java.canonicalName
    val flist = ClassGraph()
        .overrideClassLoaders(classLoader)
        .enableAllInfo()
        //.acceptPackages(pkgName) not working
        .scan()
        .use { scanResult ->
            scanResult.getClassesWithMethodAnnotation(annotationName).flatMap { routeClassInfo ->
                routeClassInfo.methodInfo.filter { it.hasAnnotation(annotation.java) }
            }
        }
    return flist
}

@Throws(Exception::class)
fun <T : Annotation>getFileAnnotatedWith(classLoader: ClassLoader, annotation: KClass<out T>): List<T> {
    val pkgName = annotation.java.`package`.name
    val annotationName = annotation.java.canonicalName
    val aList = ClassGraph()
        .overrideClassLoaders(classLoader)
        .enableAllInfo()
        //.acceptPackages(pkgName) not working
        .scan()
        .use { scanResult ->
            scanResult.getClassesWithAnnotation(annotationName).flatMap { routeClassInfo ->
                routeClassInfo.annotationInfo
                    .filter { it.name == annotationName }
                    .mapNotNull { it.loadClassAndInstantiate() as T }
            }
        }
    return aList
}