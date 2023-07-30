plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudCore)
    compileOnly(project(":cloud-annotations"))
    compileOnly(libs.jda5)
}
