import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("cloud.base-conventions")
    id("com.palantir.revapi")
}

dependencies {
    implementation(projects.cloudCore)

    testImplementation(libs.compileTesting)
}

tasks.withType(JavaCompile::class).configureEach {
    options.errorprone {
        disable("UnusedMethod") // false positives from command annotations
    }
}
