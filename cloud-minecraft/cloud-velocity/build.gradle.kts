plugins {
    id("cloud.base-conventions")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    compileOnly(projects.cloudMinecraftExtras)
    compileOnly(libs.velocityApi)
}
