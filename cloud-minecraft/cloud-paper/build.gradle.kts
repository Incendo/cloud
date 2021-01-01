dependencies {
    api(project(":cloud-bukkit"))
    compileOnly("com.destroystokyo.paper", "paper-api", Versions.paperApi)
    compileOnly("com.destroystokyo.paper", "paper-mojangapi", Versions.paperApi)
    compileOnly("org.jetbrains", "annotations", Versions.jetbrainsAnnotations)
    compileOnly("com.google.guava", "guava", Versions.guava)
}
