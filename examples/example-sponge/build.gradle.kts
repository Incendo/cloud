import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "2.1.1"
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":cloud-sponge"))
    implementation(project(":cloud-minecraft-extras"))
}

sponge {
    injectRepositories(false)
    apiVersion("8.0.0")
    plugin("cloud-example-sponge") {
        loader {
            name(PluginLoaders.JAVA_PLAIN)
            version("1.0")
        }
        displayName("Cloud example Sponge plugin")
        description("Plugin to demonstrate and test the Sponge implementation of cloud")
        license("MIT")
        entrypoint("cloud.commandframework.examples.sponge.CloudExamplePlugin")
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
}

configurations {
    spongeRuntime {
        resolutionStrategy.cacheChangingModulesFor(1, "MINUTES")
    }
}
