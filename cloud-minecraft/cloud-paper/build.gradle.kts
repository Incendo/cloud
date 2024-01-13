plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    api(projects.cloudBukkit)
    compileOnly(libs.paperApi)
    compileOnly(libs.paperMojangApi)
    compileOnly(libs.jetbrainsAnnotations)
}
