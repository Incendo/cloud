dependencies {
    api(projects.cloudCore)
    api(projects.cloudBrigadier)
    api(projects.cloudTasks)
    compileOnly("org.bukkit", "bukkit", Versions.bukkit)
    compileOnly("me.lucko", "commodore", Versions.commodore)
    compileOnly("org.jetbrains", "annotations", Versions.jetbrainsAnnotations)
    compileOnly("com.google.guava", "guava", Versions.guava)
}
