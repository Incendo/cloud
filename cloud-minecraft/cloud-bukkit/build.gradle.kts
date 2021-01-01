dependencies {
    api(project(":cloud-core"))
    api(project(":cloud-brigadier"))
    api(project(":cloud-tasks"))
    compileOnly("org.bukkit", "bukkit", versions["bukkit"])
    compileOnly("me.lucko", "commodore", versions["commodore"])
    compileOnly("org.jetbrains", "annotations", versions["jb-annotations"])
    compileOnly("com.google.guava", "guava", versions["guava"])
}
