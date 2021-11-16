plugins {
    id("cloud.kotlin-conventions")
}

dependencies {
    api(project(":cloud-core"))
    api(libs.bundles.coroutines)

    compileOnly(project(":cloud-kotlin-extensions"))
    testImplementation(project(":cloud-kotlin-extensions"))
}
