plugins {
    id("cloud.kotlin-conventions")
    id("cloud.publishing-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(libs.bundles.coroutines)

    compileOnly(projects.cloudKotlinExtensions)
    testImplementation(projects.cloudKotlinExtensions)
}
