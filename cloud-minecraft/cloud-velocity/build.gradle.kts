plugins {
    id("cloud.base-conventions")
}

java {
    disableAutoTargetJvm()
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    compileOnly(libs.velocityApi)
}
