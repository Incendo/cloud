import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform

plugins {
    id("cloud.base-conventions")
    id("org.spongepowered.gradle.vanilla")
}

dependencies {
    api(project(":cloud-core"))
    implementation(project(":cloud-brigadier"))
    compileOnly("org.spongepowered:spongeapi:8.1.0")
    compileOnly("org.spongepowered:sponge:1.16.5-8.1.0-SNAPSHOT")
}

minecraft {
    version("1.16.5")
    platform(MinecraftPlatform.JOINED)
}
