plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    //androidTarget()

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":hot_preview:annotation"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
            }
        }

        wasmJs { browser() }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(project(":hot_preview:hot_reload"))
            }
        }
    }
}


/*val runTasks = tasks.findByName("desktopRun")
println(runTasks)

//TODO
tasks.create("preview") {
    group = "preview"
    description = "Run the preview"
    val sourceSet = sourceSets.findByName("desktopMain")
    doLast {
        if (sourceSet != null) {
            tasks.create("previewRun", JavaExec::class) {
                group = "preview"
                description = "Run the preview"
                classpath = sourceSet.runtimeClasspath
                mainClass = "de.drick.compose.hotpreview.Cli_mainKt"
            }
        }
    }
}
*/