plugins {
    alias(libs.plugins.cloud.buildLogic.rootProject.publishing)
    alias(libs.plugins.cloud.buildLogic.rootProject.spotless)
    alias(libs.plugins.versions)
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
