plugins {
    `kotlin-dsl`
    alias(libs.plugins.cloud.buildLogic.spotless)
}

repositories {
    gradlePluginPortal()
    mavenLocal()
}

dependencies {
    // loom needs this version of asm, for some reason we have an older one on the classpath without this
    implementation("org.ow2.asm:asm:9.6")
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
plugin("example")

fun plugin(name: String) {
    val prefixedId = "cloud.$name-conventions"
    gradlePlugin.plugins.register(name) {
        id = prefixedId
        implementationClass = "Cloud${name.replaceFirstChar(Char::uppercase)}Conventions"
    }
}
