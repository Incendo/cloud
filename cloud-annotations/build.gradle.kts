import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
    id("org.incendo.cloud-build-logic.revapi")
}

dependencies {
    api(projects.cloudCore)

    testImplementation(libs.compileTesting)
}

tasks.withType(JavaCompile::class).configureEach {
    options.errorprone {
        disable("UnusedMethod") // false positives from command annotations
    }
}
