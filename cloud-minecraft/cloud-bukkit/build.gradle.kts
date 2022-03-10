plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    api(projects.cloudTasks)
    compileOnly(libs.bukkit)
    compileOnly(libs.commodore)
    compileOnly(libs.jetbrainsAnnotations)
    compileOnly(libs.guava)
}
