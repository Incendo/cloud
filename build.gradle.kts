import com.hierynomus.gradle.license.LicenseBasePlugin
import de.marcphilipp.gradle.nexus.NexusPublishPlugin
import net.ltgt.gradle.errorprone.ErrorPronePlugin
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    signing
    id("checkstyle")
    id("com.github.hierynomus.license") version "0.15.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("de.marcphilipp.nexus-publish") version "0.4.0"
    id("net.ltgt.errorprone") version "1.3.0"
}

checkstyle {
    configFile = file("config/checkstyle/checkstyle.xml")
}

buildGroups("Minecraft", "Discord", "IRC")

gradle.taskGraph.whenReady {
    gradle.taskGraph.allTasks.forEach {
        if (it.project.name.contains("example")) {
            it.onlyIf {
                project.hasProperty("compile-examples")
            }
        }
    }
}

allprojects {
    apply<IdeaPlugin>()
    apply<CheckstylePlugin>()
    apply<LicenseBasePlugin>()

    group = "cloud.commandframework"
    version = "1.4.0"
    description = "Command framework and dispatcher for the JVM"

    /* Disable checkstyle on tests */
    project.gradle.startParameter.excludedTaskNames.add("checkstyleTest")

    license {
        header = rootProject.file("HEADER")
        mapping("java", "DOUBLESLASH_STYLE")
        mapping("kt", "DOUBLESLASH_STYLE")
        includes(listOf("**/*.java", "**/*.kt"))
    }
}

subprojects {
    apply<JavaLibraryPlugin>()
    apply<SigningPlugin>()
    apply<NexusPublishPlugin>()
    apply<ErrorPronePlugin>()

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
        withJavadocJar()
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
        withType<JavaCompile> {
            options.encoding = Charsets.UTF_8.name()
            options.compilerArgs.addAll(setOf("-Xlint:all", "-Xlint:-processing", "-Werror"))
            options.errorprone {
                /* These are just annoying */
                disable(
                        "JdkObsolete",
                        "FutureReturnValueIgnored",
                        "ImmutableEnumChecker",
                        "StringSplitter",
                        "EqualsGetClass",
                        "CatchAndPrintStackTrace"
                )
            }
        }
        withType<Javadoc> {
            options.encoding = Charsets.UTF_8.name()
        }
        build {
            dependsOn(checkstyleMain)
            dependsOn(licenseMain)
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        /* Sonatype Snapshots */
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        /* ViaVersion, used for adventure */
        maven("https://repo.viaversion.com/")
        /* Velocity, used for cloud-velocity */
        maven("https://repo.velocitypowered.com/snapshots/")
        /* The Minecraft repository, used for cloud-brigadier */
        maven("https://libraries.minecraft.net/")
        /* The current Sponge repository */
        maven("https://repo-new.spongepowered.org/repository/maven-public/")
        /* The Spigot repository, used for cloud-bukkit */
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        /* The paper repository, used for cloud-paper */
        maven("https://papermc.io/repo/repository/maven-public/")
        /* The NukkitX repository, used for cloud-cloudburst */
        maven("https://repo.nukkitx.com/maven-snapshots")
        /* JitPack, used for random dependencies */
        maven("https://jitpack.io")
    }

    dependencies {
        compileOnly("org.checkerframework", "checker-qual", Versions.checkerQual)
        api("io.leangen.geantyref", "geantyref", Versions.geantyref)
        testImplementation("org.junit.jupiter", "junit-jupiter-engine", Versions.jupiterEngine)
        errorprone("com.google.errorprone", "error_prone_core", Versions.errorprone)
        errorproneJavac("com.google.errorprone", "javac", Versions.errorprone_javac)
        compileOnly("com.google.errorprone", "error_prone_annotations", Versions.errorprone)
    }

    nexusPublishing {
        repositories {
            sonatype()
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    url.set("https://github.com/Incendo/cloud")
                    description.set(project.description)

                    developers {
                        developer {
                            id.set("Sauilitired")
                            name.set("Alexander Söderberg")
                            url.set("https://alexander-soderberg.com")
                            email.set("contact@alexander-soderberg.com")
                        }
                    }

                    issueManagement {
                        system.set("GitHub Issues")
                        url.set("https://github.com/Incendo/cloud/issues")
                    }

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    scm {
                        connection.set("scm:git@github.com:Incendo/cloud.git")
                        developerConnection.set("scm:git@github.com:Incendo/cloud.git")
                        url.set("https://github.com/Incendo/cloud/")
                    }
                }
            }
        }
    }

    signing {
        gradle.taskGraph.whenReady {
            isRequired = project.hasProperty("signing.keyId")
                        && (gradle.taskGraph.hasTask(":publish")
                        || gradle.taskGraph.hasTask(":publishToSonatype")
                        || gradle.taskGraph.hasTask(":publishToMavenLocal"))
        }
        sign(publishing.publications["mavenJava"])
    }

}
