plugins {
    id("cloud.parent-build-logic")
    alias(libs.plugins.versions)
}

subprojects {
    if (!name.startsWith("example-")) {
        apply(plugin = "cloud.publishing-conventions")
    }
}

spotlessPredeclare {
    kotlin { ktlint(libs.versions.ktlint.get()) }
    kotlinGradle { ktlint(libs.versions.ktlint.get()) }
}

tasks {
    spotlessCheck {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessCheck"))
    }
    spotlessApply {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessApply"))
    }
}
