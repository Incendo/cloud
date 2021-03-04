import com.hierynomus.gradle.license.LicenseBasePlugin
import com.hierynomus.gradle.license.tasks.LicenseCheck
import net.kyori.indra.IndraExtension
import net.kyori.indra.sonatypeSnapshots
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.gradle.api.plugins.JavaPlugin.*

plugins {
    val indraVersion = "1.2.1"
    id("net.kyori.indra") version indraVersion apply false
    id("net.kyori.indra.checkstyle") version indraVersion apply false
    id("net.kyori.indra.publishing.sonatype") version indraVersion apply false
    id("com.github.hierynomus.license") version "0.15.0" apply false
    id("com.github.johnrengelman.shadow") version "6.1.0" apply false
    id("net.ltgt.errorprone") version "1.3.0" apply false
    id("com.github.ben-manes.versions") version "0.38.0"
}

buildGroups("Minecraft", "Discord", "IRC")

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
version = "1.4.0"
description = "Command framework and dispatcher for the JVM"

subprojects {
    plugins.apply("net.kyori.indra")
    plugins.apply("net.kyori.indra.checkstyle")
    plugins.apply("net.kyori.indra.publishing.sonatype")
    apply<ErrorPronePlugin>()
    apply<LicenseBasePlugin>()

    extensions.configure(LicenseExtension::class) {
        header = rootProject.file("HEADER")
        mapping("java", "DOUBLESLASH_STYLE")
        mapping("kt", "DOUBLESLASH_STYLE")
        includes(listOf("**/*.java", "**/*.kt"))
    }

    extensions.configure(IndraExtension::class) {
        github("Incendo", "cloud") {
            ci = true
        }
        mitLicense()

        javaVersions {
            testWith(8, 11, 15)
        }
        checkstyle.set("8.39")

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

        named("check") {
            dependsOn(withType(LicenseCheck::class))
        }
    }

    repositories {
        mavenCentral()
        sonatypeSnapshots()
        jcenter()
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
    }

    dependencies {
        COMPILE_ONLY_API_CONFIGURATION_NAME("org.checkerframework", "checker-qual", Versions.checkerQual)
        TEST_IMPLEMENTATION_CONFIGURATION_NAME("org.junit.jupiter", "junit-jupiter-engine", Versions.jupiterEngine)
        "errorprone"("com.google.errorprone", "error_prone_core", Versions.errorprone)
        COMPILE_ONLY_API_CONFIGURATION_NAME("com.google.errorprone", "error_prone_annotations", Versions.errorprone)
    }

}
