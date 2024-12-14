package de.drick.compose.hot_preview

import androidx.compose.runtime.reflect.asComposableMethod
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.live_composable.HotPreviewFunction
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.kotlinFunction

fun analyzeClass(clazz: Class<*>): List<HotPreviewFunction> {
    return clazz.declaredMethods.mapNotNull { it.kotlinFunction }
        .mapNotNull { function ->
            val annotations = function.annotations
                .filterIsInstance<HotPreview>()
            val composableMethod = function.javaMethod?.let {
                it.isAccessible = true
                it.asComposableMethod()
            }
            if (annotations.isEmpty() || composableMethod == null)
                null
            else {
                HotPreviewFunction(
                    name = function.name,
                    annotation = annotations,
                    composableMethod = composableMethod
                )
            }
        }
}
