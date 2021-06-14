plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.indraCommon)
    implementation(libs.indraPublishingSonatype)
    implementation(libs.testLoggerPlugin)
    implementation(libs.errorpronePlugin)
    implementation(libs.licenser)
}
