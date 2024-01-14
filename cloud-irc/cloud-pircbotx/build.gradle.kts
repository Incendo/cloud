plugins {
    id("cloud.base-conventions")
    // id("cloud.publishing-conventions") // Don't publish for now, needs to be moved to a separate repo
}

dependencies {
    api(projects.cloudCore)
    implementation(libs.pircbotx)
}
