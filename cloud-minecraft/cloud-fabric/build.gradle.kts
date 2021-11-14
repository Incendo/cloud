import net.fabricmc.loom.task.AbstractRunTask
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("quiet-fabric-loom") version "0.10-SNAPSHOT"
}

tasks {
    compileJava {
        options.errorprone {
            excludedPaths.set(".*[/\\\\]mixin[/\\\\].*")
        }
    }

    withType<ProcessResources> {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<Javadoc> {
        (options as? StandardJavadocDocletOptions)?.apply {
            //links("https://maven.fabricmc.net/docs/yarn-${Versions.fabricMc}+build.${Versions.fabricYarn}/") // todo
        }
    }

    withType<AbstractRunTask> {
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
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc", "fabric-loader", Versions.fabricLoader)
    modImplementation(fabricApi.module("fabric-command-api-v1", Versions.fabricApi))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", Versions.fabricApi))

    modApi(include("me.lucko", "fabric-permissions-api", "0.1-SNAPSHOT"))

    api(include(project(":cloud-core"))!!)
    api(include(project(":cloud-brigadier"))!!)
    api(include(project(":cloud-services"))!!)

    api(include("io.leangen.geantyref", "geantyref", Versions.geantyref))
}

/* set up a testmod source set */
val testmod: SourceSet by sourceSets.creating {
    val main = sourceSets.main.get()
    compileClasspath += main.compileClasspath
    runtimeClasspath += main.runtimeClasspath
    dependencies.add(implementationConfigurationName, main.output)
}

val testmodJar by tasks.registering(Jar::class) {
    archiveClassifier.set("testmod-dev")
    group = LifecycleBasePlugin.BUILD_GROUP
    from(testmod.output)
}

tasks.withType<AbstractRunTask> {
    classpath(testmodJar)
}
/* end of testmod setup */
