plugins {
    kotlin("jvm")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(project(":hotpreview_prototype:hot_preview_annotation"))

    implementation(compose.desktop.common)
    implementation(compose.runtime)
    implementation(kotlin("reflect"))
    implementation(kotlin("compiler-embeddable"))
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
    testImplementation(kotlin("test"))
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
    val destinationFolder = file("$generatedResources/compilerPlugins")
    println(composePluginPath)

    from(composePluginPath)
    into(destinationFolder)

    doFirst {
        delete(destinationFolder)
    }
}
