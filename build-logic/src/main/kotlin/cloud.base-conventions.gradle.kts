import com.diffplug.gradle.spotless.FormatExtension
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
                "InlineMeSuggester"
            )
        }
        options.compilerArgs.addAll(listOf("-Xlint:-processing", "-Werror"))
    }
}

spotless {
    fun FormatExtension.applyCommon(spaces: Int = 4) {
        indentWithSpaces(spaces)
        trimTrailingWhitespace()
        endWithNewline()
    }
    java {
        licenseHeaderFile(rootProject.file("HEADER"))
        importOrderFile(rootProject.file(".spotless/cloud.importorder"))
        applyCommon()
    }
    kotlin {
        licenseHeaderFile(rootProject.file("HEADER"))
        applyCommon()
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
    }
    format("configs") {
        target("**/*.yml", "**/*.yaml", "**/*.json")
        targetExclude("run/**")
        applyCommon(2)
    }
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
