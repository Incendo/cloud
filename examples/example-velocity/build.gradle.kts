plugins {
    alias(libs.plugins.shadow)
    id("cloud.example-conventions")
    alias(libs.plugins.run.velocity)
}

indra {
    javaVersions().target(11) // Velocity 3 requires Java 11
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("com.velocitypowered:velocity-api"))
        }
    }
    assemble {
        dependsOn(shadowJar)
    }
    runVelocity {
        velocityVersion(libs.versions.velocityApi.get())
    }
}

dependencies {
    api(project(":cloud-velocity"))
    api(project(":cloud-minecraft-extras"))
    api(project(":cloud-annotations"))
    annotationProcessor(libs.velocityApi)
    compileOnly(libs.velocityApi)
}
