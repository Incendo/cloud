plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
}

dependencies {
    implementation(projects.cloudCore)
    /* Needs to be provided by the platform */
    compileOnly(libs.brigadier)
    testImplementation(libs.brigadier)
}
