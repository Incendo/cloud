plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudCore)
    implementation(libs.javacord)
}
