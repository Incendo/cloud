plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.indraCommon)
    implementation(libs.indraPublishingSonatype)
    implementation(libs.gradleTestLogger)
    implementation(libs.gradleErrorprone)
    implementation(libs.licenser)
    implementation(libs.gradleKotlinJvm)
    implementation(libs.gradleDokka)
    implementation(libs.gradleKtlint)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
