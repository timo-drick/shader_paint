plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

/*kotlin {
    jvmToolchain(21)
}*/

dependencies {
    implementation(project(":hot_preview:annotation"))

    implementation(compose.desktop.common)
    implementation(compose.runtime)
    implementation(kotlin("reflect"))
    implementation(kotlin("compiler-embeddable"))
    implementation("io.github.classgraph:classgraph:4.8.179")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
}

val generatedResources = "${layout.buildDirectory.get()}/resources/main"

sourceSets {
    main {
        output.dir(generatedResources, "builtBy" to "copyComposeCompiler")
    }
}

tasks.register<Copy>("copyComposeCompiler") {
    val pluginClasspath = project.configurations.kotlinCompilerPluginClasspathMain
    val composePluginPath = pluginClasspath.get().files.single {
        it.toString().contains("compose-compiler-plugin")
    }
    println(composePluginPath)

    from(composePluginPath)
    into(file("$generatedResources/compilerPlugins"))
}
