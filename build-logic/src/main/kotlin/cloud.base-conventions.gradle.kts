import net.kyori.indra.repository.sonatypeSnapshots
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("com.diffplug.spotless")
    id("net.ltgt.errorprone")
}

indra {
    javaVersions {
        minimumToolchain(17)
        target(8)
        testWith(8, 11, 17)
    }

    checkstyle(libs.versions.checkstyle.get())
}

/* Disable checkstyle on tests */
project.gradle.startParameter.excludedTaskNames.add("checkstyleTest")

tasks {
    withType<JavaCompile> {
        options.errorprone {
            /* These are just annoying */
            disable(
                "JdkObsolete",
                "FutureReturnValueIgnored",
                "ImmutableEnumChecker",
                "StringSplitter",
                "EqualsGetClass",
                "CatchAndPrintStackTrace",
                "InlineMeSuggester",
            )
        }
        options.compilerArgs.addAll(listOf("-Xlint:-processing", "-Werror"))
    }
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER"))
        importOrderFile(rootProject.file(".spotless/cloud.importorder"))
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlin {
        licenseHeaderFile(rootProject.file("HEADER"))
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("configs") {
        target("**/*.yml", "**/*.yaml", "**/*.json")
        targetExclude("run/**")
        indentWithSpaces(2)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

repositories {
    mavenCentral()
    sonatypeSnapshots()
    /* Velocity, used for cloud-velocity */
    maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-release/") {
        mavenContent {
            releasesOnly()
            includeGroup("com.velocitypowered")
        }
    }
    maven("https://nexus.velocitypowered.com/repository/velocity-artifacts-snapshots/") {
        mavenContent {
            snapshotsOnly()
            includeGroup("com.velocitypowered")
        }
    }
    /* The Minecraft repository, used for cloud-brigadier */
    maven("https://libraries.minecraft.net/") {
        mavenContent { releasesOnly() }
    }
    /* The Spigot repository, used for cloud-bukkit */
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        mavenContent { snapshotsOnly() }
    }
    /* The paper repository, used for cloud-paper */
    maven("https://repo.papermc.io/repository/maven-public/")
    /* The NukkitX repository, used for cloud-cloudburst */
    maven("https://repo.nukkitx.com/maven-snapshots") {
        mavenContent { snapshotsOnly() }
    }
    /* The current Fabric repository */
    maven("https://maven.fabricmc.net/") {
        mavenContent { includeGroup("net.fabricmc") }
    }
    /* The current Sponge repository */
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        mavenContent { includeGroup("org.spongepowered") }
    }
    /* JitPack, used for random dependencies */
    maven("https://jitpack.io") {
        content { includeGroupByRegex("com\\.github\\..*") }
    }
    /* JDA's maven repository for cloud-jda */
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    compileOnlyApi(libs.checkerQual)
    compileOnlyApi(libs.apiguardian)

    testImplementation(libs.jupiterEngine)
    testImplementation(libs.jupiterParams)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoJupiter)
    testImplementation(libs.truth)
    testImplementation(libs.truthJava8)
    errorprone(libs.errorproneCore)
    // Silences compiler warnings from guava using errorprone
    compileOnly(libs.errorproneAnnotations)
}
