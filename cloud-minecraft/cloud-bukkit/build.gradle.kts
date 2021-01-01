dependencies {
    api(project(":cloud-core"))
    api(project(":cloud-brigadier"))
    api(project(":cloud-tasks"))
    compileOnly("org.bukkit", "bukkit", Versions.bukkit)
    compileOnly("me.lucko", "commodore", Versions.commodore)
    compileOnly("org.jetbrains", "annotations", Versions.jetbrainsAnnotations)
    compileOnly("com.google.guava", "guava", Versions.guava)
}
