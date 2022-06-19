import net.kyori.indra.repository.sonatypeSnapshots
import net.ltgt.gradle.errorprone.errorprone
import org.cadixdev.gradle.licenser.header.HeaderStyle

plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.license-header")
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

license {
    header(rootProject.file("HEADER"))
    style["java"] = HeaderStyle.DOUBLE_SLASH.format
    style["kt"] = HeaderStyle.DOUBLE_SLASH.format
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
    maven("https://papermc.io/repo/repository/maven-public/")
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
    testImplementation(libs.jupiterEngine)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.truth)
    testImplementation(libs.truthJava8)
    errorprone(libs.errorproneCore)
    // Silences compiler warnings from guava using errorprone
    compileOnly(libs.errorproneAnnotations)
}
