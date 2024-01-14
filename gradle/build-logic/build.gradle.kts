plugins {
    `kotlin-dsl`
    alias(libs.plugins.cloud.buildLogic.spotless)
}

repositories {
    gradlePluginPortal()
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatypeOssSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    implementation(libs.cloud.build.logic)
    implementation(libs.gradleKotlinJvm)
    implementation(libs.gradleDokka)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

cloudSpotless {
    licenseHeaderFile.convention(null as RegularFile?)
    ktlintVersion = libs.versions.ktlint
}

plugin("base")
plugin("publishing")
plugin("kotlin")

fun plugin(name: String) {
    val prefixedId = "cloud.$name-conventions"
    gradlePlugin.plugins.register(name) {
        id = prefixedId
        implementationClass = "Cloud${name.replaceFirstChar(Char::uppercase)}Conventions"
    }
}
