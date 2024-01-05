plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
    alias(libs.plugins.revapi)
}

dependencies {
    compileOnlyApi(libs.bundles.annotations)
    api(libs.geantyref)
}
