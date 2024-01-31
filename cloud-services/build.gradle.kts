plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
    id("org.incendo.cloud-build-logic.revapi")
}

dependencies {
    compileOnlyApi(libs.bundles.annotations)
    api(libs.geantyref)
}
