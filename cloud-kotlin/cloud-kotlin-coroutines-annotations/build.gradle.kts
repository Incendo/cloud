plugins {
    id("cloud.kotlin-conventions")
}

dependencies {
    api(project(":cloud-core"))
    api(project(":cloud-annotations"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
}
