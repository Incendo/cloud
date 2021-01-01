plugins {
    id("com.github.johnrengelman.shadow")
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
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-paper"))
    implementation(project(":cloud-annotations"))
    implementation(project(":cloud-minecraft-extras"))
    /* Extras */
    implementation("me.lucko", "commodore", vers["commodore"])
    implementation("net.kyori", "adventure-platform-bukkit", vers["adventure-platform"])
    /* Bukkit */
    compileOnly("org.bukkit", "bukkit", "1.8.8-R0.1-SNAPSHOT")
}
