plugins {
    id("cloud.kotlin-conventions")
}

dependencies {
    api(project(":cloud-core"))
    api(project(":cloud-annotations"))
    api(kotlin("reflect"))
    api(libs.bundles.coroutines)
}
