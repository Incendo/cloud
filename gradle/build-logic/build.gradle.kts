plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // loom needs this version of asm, for some reason we have an older one on the classpath without this
    implementation("org.ow2.asm:asm:9.6")
    implementation(libs.indraCommon)
    implementation(libs.indraPublishingSonatype)
    implementation(libs.gradleErrorprone)
    implementation(libs.gradleKotlinJvm)
    implementation(libs.gradleDokka)
    implementation(libs.spotless)
    implementation(libs.revapi)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

spotless {
    kotlinGradle {
        target("*.gradle.kts", "src/*/kotlin/**.gradle.kts", "src/*/kotlin/**.kt")
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(
                mapOf(
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_trailing-comma-on-call-site" to "disabled",
                    "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
                )
            )

        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}
