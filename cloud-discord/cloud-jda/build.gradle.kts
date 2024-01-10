plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
}

dependencies {
    api(projects.cloudCore)
    compileOnly(libs.jda)
}
