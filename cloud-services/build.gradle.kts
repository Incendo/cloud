plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
    id("com.palantir.revapi")
}

dependencies {
    compileOnlyApi(libs.bundles.annotations)
    api(libs.geantyref)
}
