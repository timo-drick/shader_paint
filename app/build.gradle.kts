import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMetadataTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

// Define a plugin
class HelloWorldPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("helloWorld") {
            group = "Example"
            description = "Prints 'Hello, World!' to the console"
            doLast {
                println("Task: $name")
                val mppExt = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension
                val jvmTarget = mppExt.targets.withType(KotlinJvmTarget::class.java)
                println("jvm target: ${jvmTarget.names}")
                val commonTarget = mppExt.targets.withType(KotlinMetadataTarget::class.java)
                println("common target: ${commonTarget.names}")
                println("Targets: ${mppExt.targets.map { it.name }}")
                val sourceSets = mppExt.sourceSets
                println("Source sets: ${sourceSets.names}")

            }
        }
    }
}


// Apply the plugin
apply<HelloWorldPlugin>()


kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(project.projectDir.path)
                    }
                }
            }
        }
        binaries.executable()
    }

    jvm("desktop")

    //androidTarget()
    
    sourceSets {
        val desktopMain by getting
        //val androidMain by getting

        commonMain.dependencies {
            implementation(project(":hotpreview_prototype:hot_preview"))
            implementation("com.github.skydoves:colorpicker-compose:1.1.2")

            //implementation(compose.components.resources)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.foundation)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            //implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0") //Needed because of the colorpicker dependency
        }
        wasmJsMain.dependencies {
            //implementation(compose.components.resources)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        //androidMain.dependencies {
        //}
    }
}

configurations.all {
    resolutionStrategy {
        // Workaround for https://youtrack.jetbrains.com/issue/CMP-6658
        force("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
        force("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
        force("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    }
}

