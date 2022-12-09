plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // loom needs this version of asm, for some reason we have an older one on the classpath without this
    implementation("org.ow2.asm:asm:9.4")
    implementation(libs.indraCommon)
    implementation(libs.indraPublishingSonatype)
    implementation(libs.gradleTestLogger)
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
            .editorConfigOverride(mapOf("ktlint_disabled_rules" to "filename"))
    }
}
