package de.drick.compose.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class HotPreviewPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.initializePreview()
    }
}
