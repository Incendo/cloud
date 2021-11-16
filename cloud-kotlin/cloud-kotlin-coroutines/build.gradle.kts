plugins {
    id("cloud.kotlin-conventions")
}

dependencies {
    api(project(":cloud-core"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")

    compileOnly(project(":cloud-kotlin-extensions"))
    testImplementation(project(":cloud-kotlin-extensions"))
}
