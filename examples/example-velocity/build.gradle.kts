plugins {
    id ("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("com.velocitypowered:velocity-api"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    api(project(":cloud-velocity"))
    api(project(":cloud-minecraft-extras"))
    api(project(":cloud-annotations"))
    compileOnly("com.velocitypowered:velocity-api:1.1.0")
    annotationProcessor("com.velocitypowered:velocity-api:1.1.0")
}
