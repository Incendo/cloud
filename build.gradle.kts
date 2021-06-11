import io.github.gradlenexus.publishplugin.NexusPublishExtension
import net.kyori.indra.IndraLicenseHeaderPlugin
import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.IndraPublishingPlugin
import net.kyori.indra.IndraExtension
import net.kyori.indra.IndraPlugin
import net.kyori.indra.repository.sonatypeSnapshots
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone
import org.cadixdev.gradle.licenser.LicenseExtension
import org.cadixdev.gradle.licenser.header.HeaderStyle
import org.gradle.api.plugins.JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME

plugins {
    val indraVersion = "2.0.2"
    id("net.kyori.indra") version indraVersion apply false
    id("net.kyori.indra.checkstyle") version indraVersion apply false
    id("net.kyori.indra.publishing") version indraVersion apply false
    id("net.kyori.indra.publishing.sonatype") version indraVersion
    id("net.kyori.indra.license-header") version indraVersion apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("net.ltgt.errorprone") version "2.0.1" apply false
    id("com.github.ben-manes.versions") version "0.38.0"
}

//buildGroups("Minecraft", "Discord", "IRC")

gradle.taskGraph.whenReady {
    gradle.taskGraph.allTasks.forEach {
        if (it.project.name.contains("example")) {
            it.onlyIf {
                project.hasProperty("compile-examples")
            }
        }
    }
}

group = "cloud.commandframework"
version = "1.5.0-SNAPSHOT"
description = "Command framework and dispatcher for the JVM"

extensions.configure<NexusPublishExtension> {
    repositories.create("incendoSnapshots") {
        snapshotRepositoryUrl.set(uri("https://repo.incendo.org/content/repositories/snapshots/"))
        username.set(System.getenv("SNAPSHOT_PUBLISHING_USERNAME"))
        password.set(System.getenv("SNAPSHOT_PUBLISHING_PASSWORD"))
    }
}

subprojects {
    apply<IndraPlugin>()
    apply<IndraPublishingPlugin>()
    apply<IndraCheckstylePlugin>()
    apply<IndraLicenseHeaderPlugin>()
    apply<ErrorPronePlugin>()

    extensions.configure(IndraExtension::class) {
        github("Incendo", "cloud") {
            ci(true)
        }
        mitLicense()

        javaVersions {
            testWith(8, 11, 16)
        }

        checkstyle("8.39")

        configurePublications {
            pom {
                developers {
                    developer {
                        id.set("Sauilitired")
                        name.set("Alexander Söderberg")
                        url.set("https://alexander-soderberg.com")
                        email.set("contact@alexander-soderberg.com")
                    }
                }
            }
        }
    }

    /* Disable checkstyle on tests */
    project.gradle.startParameter.excludedTaskNames.add("checkstyleTest")

    tasks {
        withType(JavaCompile::class) {
            options.errorprone {
                /* These are just annoying */
                disable(
                        "JdkObsolete",
                        "FutureReturnValueIgnored",
                        "ImmutableEnumChecker",
                        "StringSplitter",
                        "EqualsGetClass",
                        "CatchAndPrintStackTrace"
                )
            }
            options.compilerArgs.addAll(listOf("-Xlint:-processing", "-Werror"))
        }
    }

    extensions.configure<LicenseExtension> {
        header(rootProject.file("HEADER"))
        style["java"] = HeaderStyle.DOUBLE_SLASH.format
        style["kt"] = HeaderStyle.DOUBLE_SLASH.format
    }

    repositories {
        mavenCentral()
        sonatypeSnapshots()
        /* Velocity, used for cloud-velocity */
        maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-release/") {
            mavenContent { releasesOnly() }
        }
        /* The Minecraft repository, used for cloud-brigadier */
        maven("https://libraries.minecraft.net/") {
            mavenContent { releasesOnly() }
        }
        /* The current Sponge repository */
        maven("https://repo-new.spongepowered.org/repository/maven-public/")
        /* The Spigot repository, used for cloud-bukkit */
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            mavenContent { snapshotsOnly() }
        }
        /* The paper repository, used for cloud-paper */
        maven("https://papermc.io/repo/repository/maven-public/")
        /* The NukkitX repository, used for cloud-cloudburst */
        maven("https://repo.nukkitx.com/maven-snapshots") {
            mavenContent { snapshotsOnly() }
        }
        /* JitPack, used for random dependencies */
        maven("https://jitpack.io") {
            content { includeGroupByRegex("com\\.github\\..*") }
        }
        /* JDA's maven repository for cloud-jda */
        maven("https://m2.dv8tion.net/releases")
        // todo - temp for commodore snapshot
        maven("https://repo.incendo.org/content/repositories/snapshots/") {
            mavenContent {
                snapshotsOnly()
                includeGroup("me.lucko")
            }
        }
    }

    dependencies {
        COMPILE_ONLY_API_CONFIGURATION_NAME("org.checkerframework", "checker-qual", Versions.checkerQual)
        TEST_IMPLEMENTATION_CONFIGURATION_NAME("org.junit.jupiter", "junit-jupiter-engine", Versions.jupiterEngine)
        "errorprone"("com.google.errorprone", "error_prone_core", Versions.errorprone)
        COMPILE_ONLY_API_CONFIGURATION_NAME("com.google.errorprone", "error_prone_annotations", Versions.errorprone)
    }

}
