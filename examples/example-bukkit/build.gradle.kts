import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("cloud.example-conventions")
    id("com.github.johnrengelman.shadow")
    id("xyz.jpenilla.run-paper")
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-paper"))
    implementation(project(":cloud-annotations"))
    implementation(project(":cloud-minecraft-extras"))
    /* Extras */
    implementation(libs.commodore) {
        isTransitive = false
    }
    implementation(libs.adventurePlatformBukkit)
    /* Bukkit */
    compileOnly(libs.bukkit)
    /* Annotation processing */
    annotationProcessor(project(":cloud-annotations"))
}

tasks {
    shadowJar {
        relocate("net.kyori", "cloud.commandframework.example.kyori")
        relocate("me.lucko", "cloud.commandframework.example.lucko")
        relocate("io.leangen.geantyref", "cloud.commandframework.example.geantyref")
    }
    assemble {
        dependsOn(shadowJar)
    }
    runServer {
        minecraftVersion("1.19.2")
        runDirectory(file("run/latest"))
        javaLauncher.set(
            project.javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        )
    }

    // Set up a run task for each supported version
    mapOf(
        8 to setOf("1.8.8"),
        11 to setOf("1.9.4", "1.10.2", "1.11.2"),
        17 to setOf("1.12.2", "1.13.2", "1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2", "1.19.2")
    ).forEach { (javaVersion, minecraftVersions) ->
        for (version in minecraftVersions) {
            createVersionedRun(version, javaVersion)
        }
    }
}

fun TaskContainerScope.createVersionedRun(
    version: String,
    javaVersion: Int
) = register<RunServer>("runServer${version.replace(".", "_")}") {
    group = "cloud"
    pluginJars.from(shadowJar.flatMap { it.archiveFile })
    minecraftVersion(version)
    runDirectory(file("run/$version"))
    systemProperty("Paper.IgnoreJavaVersion", true)
    javaLauncher.set(
        project.javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    )
}
