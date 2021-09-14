plugins {
    `kotlin-dsl`
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
}
