dependencies {
    api(project(":cloud-core"))
    api(project(":cloud-brigadier"))
    implementation("org.spongepowered:spongeapi:8.0.0-SNAPSHOT")
    compileOnly("org.spongepowered:sponge:1.16.5-8.0.0-SNAPSHOT")
    implementation("com.mojang:brigadier:1.0.17")
}
