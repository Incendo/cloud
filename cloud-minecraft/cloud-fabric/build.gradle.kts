import net.fabricmc.loom.task.AbstractRunTask
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("quiet-fabric-loom")
    id("cloud.base-conventions")
}

indra {
    javaVersions().target(17)
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
            // links("https://maven.fabricmc.net/docs/yarn-${Versions.fabricMc}+build.${Versions.fabricYarn}/") // todo
        }
    }

    withType<AbstractRunTask> {
        standardInput = System.`in`
        jvmArgumentProviders += CommandLineArgumentProvider {
            if (System.getProperty("idea.active")?.toBoolean() == true || // IntelliJ
                System.getenv("TERM") != null || // linux terminals
                System.getenv("WT_SESSION") != null
            ) { // Windows terminal
                listOf("-Dfabric.log.disableAnsi=false")
            } else {
                listOf()
            }
        }
    }
}

dependencies {
    minecraft(libs.fabricMinecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabricLoader)
    modImplementation(fabricApi.module("fabric-command-api-v2", libs.versions.fabricApi.get()))
    modImplementation(fabricApi.module("fabric-networking-api-v1", libs.versions.fabricApi.get()))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", libs.versions.fabricApi.get()))

    modImplementation(libs.fabricPermissionsApi)
    include(libs.fabricPermissionsApi)

    api(include(projects.cloudCore)!!)
    api(include(projects.cloudBrigadier)!!)
    api(include(projects.cloudServices)!!)

    api(libs.geantyref)
    include(libs.geantyref)
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
