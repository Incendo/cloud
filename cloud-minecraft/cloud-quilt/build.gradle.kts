import net.fabricmc.loom.task.AbstractRunTask
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("fabric-loom") version "0.7-SNAPSHOT"
}

/* set up a testmod source set */
val testmod by sourceSets.creating {
    val main = sourceSets.main.get()
    compileClasspath += main.compileClasspath
    runtimeClasspath += main.runtimeClasspath
    dependencies.add(implementationConfigurationName, main.output)
}

val testmodJar by tasks.creating(Jar::class) {
    archiveClassifier.set("testmod-dev")
    group = LifecycleBasePlugin.BUILD_GROUP
    from(testmod.output)
}

loom.unmappedModCollection.from(testmodJar)

/* end of testmod setup */

tasks {
    compileJava {
        options.errorprone {
            excludedPaths.set(".*[/\\\\]mixin[/\\\\].*")
        }
    }

    withType(ProcessResources::class).configureEach {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType(Javadoc::class).configureEach {
        (options as? StandardJavadocDocletOptions)?.apply {
            links("https://maven.fabricmc.net/docs/yarn-${Versions.fabricMc}+build.${Versions.fabricYarn}/")
        }
    }

    withType(AbstractRunTask::class).configureEach {
        standardInput = System.`in`
        jvmArgumentProviders += CommandLineArgumentProvider {
            if (System.getProperty("idea.active")?.toBoolean() == true || // IntelliJ
                    System.getenv("TERM") != null || // linux terminals
                    System.getenv("WT_SESSION") != null) { // Windows terminal
                listOf("-Dfabric.log.disableAnsi=false")
            } else {
                listOf()
            }
        }
    }
}


dependencies {
    minecraft("com.mojang", "minecraft", Versions.fabricMc)
    mappings("net.fabricmc", "yarn", "${Versions.fabricMc}+build.${Versions.fabricYarn}", classifier = "v2")
    modImplementation("net.fabricmc", "fabric-loader", Versions.fabricLoader)
    modImplementation(fabricApi.module("fabric-command-api-v1", Versions.fabricApi))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", Versions.fabricApi))

    modApi(include("me.lucko", "fabric-permissions-api", "0.1-SNAPSHOT"))

    api(include(project(":cloud-core"))!!)
    api(include(project(":cloud-brigadier"))!!)
    api(include(project(":cloud-services"))!!)

    api(include("io.leangen.geantyref", "geantyref", Versions.geantyref))
}

indra {
    includeJavaSoftwareComponentInPublications.set(false)
    configurePublications {
        // add all the jars that should be included when publishing to maven
        artifact(tasks.remapJar) {
            builtBy(tasks.remapJar)
        }
        artifact(tasks.sourcesJar) {
            builtBy(tasks.remapSourcesJar)
        }

        // Loom is broken with project dependencies in the same build (because it resolves dependencies during configuration)
        // Please look away
        pom {
            withXml {
                val dependencies = asNode().appendNode("dependencies")
                sequenceOf("brigadier", "core", "services").forEach {
                    val depNode = dependencies.appendNode("dependency")
                    depNode.appendNode("groupId", project.group)
                    depNode.appendNode("artifactId", "cloud-$it")
                    depNode.appendNode("version", project.version)
                    depNode.appendNode("scope", "compile")
                }
            }
        }
    }
}
