import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version Versions.composeDesktop
    kotlin("plugin.compose")
    //id("com.google.devtools.ksp")
}

group = "de.appsonair.compose"
version = "1.0-SNAPSHOT"

dependencies {

    /*
    val version = "0.3.3-1.7.1"
    implementation("de.drick.compose:hotreload:$version")
    implementation("de.drick.compose:hotreload-ksp-processor:$version")
    ksp("de.drick.compose:hotreload-ksp-processor:$version")
    */

    implementation("com.github.skydoves:colorpicker-compose:1.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0") //Needed because of the colorpicker dependency

    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.common)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.runtime)
    implementation(compose.foundation)

}

kotlin {
    jvmToolchain(17)
}


compose.desktop {
    application {
        mainClass = "de.drick.compose.shaderpaint.Main_desktop_appKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "de.drick.compose.shaderpaint"
            packageVersion = "1.0.0"
            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("mango_icon.png"))
            }
        }
    }
}

