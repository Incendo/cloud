plugins {
    id("cloud.example-conventions")
    id ("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("net.md-5:bungeecord-api:1.8-SNAPSHOT"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    /* Cloud */
    implementation(project(":cloud-bungee"))
    implementation(project(":cloud-annotations"))
    implementation(project(":cloud-minecraft-extras"))
    /* Extras */
    implementation(libs.adventurePlatformBungeecord)
    /* Bungee*/
    compileOnly("net.md-5", "bungeecord-api", "1.8-SNAPSHOT")
}
