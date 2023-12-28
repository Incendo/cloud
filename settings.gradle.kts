enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") {
            name = "fabric"
        }
        maven("https://repo.jpenilla.xyz/snapshots/") {
            name = "jmpSnapshots"
            mavenContent { snapshotsOnly() }
        }
    }
    includeBuild("build-logic")
}

plugins {
    id("quiet-fabric-loom") version "1.4-SNAPSHOT"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatypeOssSnapshots"
            mavenContent { snapshotsOnly() }
        }
        /* The Minecraft repository, used for cloud-brigadier */
        maven("https://libraries.minecraft.net/") {
            name = "minecraftLibraries"
            mavenContent {
                releasesOnly()
                includeGroup("com.mojang")
                includeGroup("net.minecraft")
            }
        }
        /* The paper repository, used for cloud-paper */
        maven("https://repo.papermc.io/repository/maven-public/")
        /* Used for cloud-cloudburst */
        maven("https://repo.opencollab.dev/maven-snapshots/") {
            name = "cloudburst"
            mavenContent {
                snapshotsOnly()
                includeGroup("org.cloudburstmc")
            }
        }
        /* The current Fabric repository */
        maven("https://maven.fabricmc.net/") {
            name = "fabric"
            mavenContent { includeGroup("net.fabricmc") }
        }
        /* The current Sponge repository */
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "sponge"
            mavenContent { includeGroup("org.spongepowered") }
        }
        /* JitPack, used for random dependencies */
        maven("https://jitpack.io") {
            name = "jitpack"
            content { includeGroupByRegex("com\\.github\\..*") }
        }
        /* JDA's maven repository for cloud-jda */
        maven("https://m2.dv8tion.net/releases") {
            name = "dv8tion"
            mavenContent { releasesOnly() }
        }
    }
}

rootProject.name = "cloud"

include(":cloud-bom")

// Core Modules
include(":cloud-core")
include(":cloud-services")
include(":cloud-annotations")

// Kotlin Extensions
setupKotlinModule("cloud-kotlin-extensions")
setupKotlinModule("cloud-kotlin-coroutines")
setupKotlinModule("cloud-kotlin-coroutines-annotations")

// Discord Modules
setupDiscordModule("cloud-javacord")
setupDiscordModule("cloud-jda")

// Minecraft Modules
setupMinecraftModule("cloud-brigadier")
setupMinecraftModule("cloud-bukkit")
setupMinecraftModule("cloud-paper")
setupMinecraftModule("cloud-bungee")
setupMinecraftModule("cloud-cloudburst")
setupMinecraftModule("cloud-velocity")
//setupMinecraftModule("cloud-sponge")
setupMinecraftModule("cloud-sponge7")
setupMinecraftModule("cloud-minecraft-extras")
setupMinecraftModule("cloud-fabric")

// IRC Modules
setupIrcModule("cloud-pircbotx")

// Example Modules
setupExampleModule("example-bukkit")
setupExampleModule("example-bungee")
setupExampleModule("example-velocity")
setupExampleModule("example-jda")

fun setupIrcModule(name: String) =
        setupSubproject(name, file("cloud-irc/$name"))

fun setupDiscordModule(name: String) =
        setupSubproject(name, file("cloud-discord/$name"))

fun setupMinecraftModule(name: String) =
        setupSubproject(name, file("cloud-minecraft/$name"))

fun setupKotlinModule(name: String) =
        setupSubproject(name, file("cloud-kotlin/$name"))

fun setupExampleModule(name: String) =
        setupSubproject(name, file("examples/$name"))

fun setupSubproject(name: String, projectDirectory: File) = setupSubproject(name) {
    projectDir = projectDirectory
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
