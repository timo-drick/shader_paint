val pluginId = "de.drick.compose.hotprewview.plugin"

plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
}


gradlePlugin {
    plugins {
        create(pluginId) {
            id = pluginId
            implementationClass = "de.drick.compose.plugin.HotPreviewPlugin"
            version = "0.1"
            description = "Plugin to support previews of common compose code"
            displayName = "HotPreview plugin"
            // Note: tags cannot include "plugin" or "gradle" when publishing
            tags.set(listOf("HotPreview", "compose"))
        }
    }
}