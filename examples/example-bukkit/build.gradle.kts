plugins {
    id("cloud.example-conventions")
    id("com.github.johnrengelman.shadow")
    id("xyz.jpenilla.run-paper") version "1.0.3"
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-paper"))
    implementation(project(":cloud-annotations"))
    implementation(project(":cloud-minecraft-extras"))
    /* Extras */
    implementation("me.lucko", "commodore", Versions.commodore)
    implementation("net.kyori", "adventure-platform-bukkit", Versions.adventurePlatform)
    /* Bukkit */
    compileOnly("org.bukkit", "bukkit", Versions.bukkit)
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("org.bukkit:bukkit:1.8.8-R0.1-SNAPSHOT"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
    runServer {
        minecraftVersion("1.17")
    }
}
