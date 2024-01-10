plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
}

dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    compileOnly(libs.bukkit)
    compileOnly(libs.commodore)
    compileOnly(libs.jetbrainsAnnotations)
    compileOnly(libs.guava)
    testImplementation(libs.bukkit)
    testImplementation(libs.jetbrainsAnnotations)
}

spotless {
    java {
        targetExclude(file("src/main/java/cloud/commandframework/bukkit/internal/MinecraftArgumentTypes.java"))
    }
}
