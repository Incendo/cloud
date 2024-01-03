import com.diffplug.gradle.spotless.FormatExtension

plugins {
    id("com.diffplug.spotless")
}

fun FormatExtension.applyCommon(spaces: Int = 4) {
    indentWithSpaces(spaces)
    trimTrailingWhitespace()
    endWithNewline()
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER"))
        importOrderFile(rootProject.file(".spotless/cloud.importorder"))
        applyCommon()
    }
    kotlinGradle {
        ktlint(libs.versions.ktlint.get())
        applyCommon()
    }
    format("configs") {
        target("**/*.yml", "**/*.yaml", "**/*.json")
        targetExclude("run/**")
        applyCommon(2)
    }
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    spotless {
        kotlin {
            licenseHeaderFile(rootProject.file("HEADER"))
            applyCommon()

            ktlint(libs.versions.ktlint.get())
                .editorConfigOverride(
                    mapOf(
                        "ktlint_standard_filename" to "disabled",
                        "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                        "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                    )
                )
        }
    }
}
