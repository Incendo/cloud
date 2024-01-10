plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    compileOnly(libs.velocityApi)
}
