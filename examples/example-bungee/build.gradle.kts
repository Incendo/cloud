plugins {
    id("cloud.example-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.waterfall)
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("net.md-5:bungeecord-api:1.8-SNAPSHOT"))
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    runWaterfall {
        waterfallVersion("1.19")
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
