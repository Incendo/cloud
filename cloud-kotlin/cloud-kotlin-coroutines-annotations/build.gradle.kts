plugins {
    id("cloud.kotlin-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudAnnotations)
    api(kotlin("reflect"))
    api(libs.bundles.coroutines)
}
