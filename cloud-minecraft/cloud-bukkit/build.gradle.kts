plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    api(projects.cloudTasks)
    compileOnly(projects.cloudMinecraftExtras)
    compileOnly(libs.adventureApi)
    compileOnly(libs.bukkit)
    compileOnly(libs.commodore)
    compileOnly(libs.jetbrainsAnnotations)
    compileOnly(libs.guava)
}

spotless {
    java {
        targetExclude(file("src/main/java/cloud/commandframework/bukkit/internal/MinecraftArgumentTypes.java"))
    }
}
