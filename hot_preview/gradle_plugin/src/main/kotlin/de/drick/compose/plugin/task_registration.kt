package de.drick.compose.plugin

import org.gradle.api.Project

fun Project.initializePreview() {
    plugins.withId(KOTLIN_MPP_PLUGIN_ID) {
        println("Plugin: $it")
        /*
        mppExt.targets.withType(KotlinJvmTarget::class.java) { target ->
            val runtimeFilesProvider = JvmApplicationRuntimeFilesProvider.FromKotlinMppTarget(target)
            registerConfigurePreviewTask(project, runtimeFilesProvider, targetName = target.name)
        }
        */
    }
    plugins.withId(KOTLIN_JVM_PLUGIN_ID) {
        println("Plugin: $it")
        /*
        val sourceSet = project.javaSourceSets.getByName("main")
        val runtimeFilesProvider = JvmApplicationRuntimeFilesProvider.FromGradleSourceSet(sourceSet)
        registerConfigurePreviewTask(project, runtimeFilesProvider)
         */
    }
}

internal const val KOTLIN_MPP_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"
internal const val KOTLIN_JVM_PLUGIN_ID = "org.jetbrains.kotlin.jvm"
internal const val KOTLIN_ANDROID_PLUGIN_ID = "org.jetbrains.kotlin.android"
internal const val KOTLIN_JS_PLUGIN_ID = "org.jetbrains.kotlin.js"
internal const val COMPOSE_PLUGIN_ID = "org.jetbrains.compose"

internal const val IDEA_IMPORT_TASK_NAME = "prepareKotlinIdeaImport"