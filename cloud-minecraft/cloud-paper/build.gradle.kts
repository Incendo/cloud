dependencies {
    api(project(":cloud-bukkit"))
    compileOnly("com.destroystokyo.paper", "paper-api", versions["paper-api"])
    compileOnly("com.destroystokyo.paper", "paper-mojangapi", versions["paper-api"])
    compileOnly("org.jetbrains", "annotations", versions["jb-annotations"])
    compileOnly("com.google.guava", "guava", versions["guava"])
}
