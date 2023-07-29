plugins {
    id("cloud.base-conventions")
}

indra {
    javaVersions().target(17)
}

dependencies {
    api(projects.cloudCore)
    compileOnly(libs.minestom)
}
