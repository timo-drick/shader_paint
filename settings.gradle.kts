rootProject.name = "ShaderPainT"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        //mavenLocal()
    }
}

include(":app")
include(":hotpreview_prototype:hot_preview_annotation")
include(":hotpreview_prototype:hot_preview")
include(":hotpreview_prototype:hot_reload")
