import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

val compileAndTest: Configuration by configurations.creating

configurations {
    all {
        dependencies.removeIf { it.group == "org.jetbrains.kotlin" }
    }

    compileOnly {
        extendsFrom(compileAndTest)
    }
    testImplementation {
        extendsFrom(compileAndTest)
    }
}

dependencies {
    api(project(":cloud-core"))

    compileAndTest(project(":cloud-annotations"))
    compileAndTest(kotlin("stdlib-jdk8"))
    compileAndTest(kotlin("reflect"))
    compileAndTest("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    compileAndTest("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
}

tasks {
    withType<DokkaTask>().configureEach {
        dokkaSourceSets.getByName("main") {
            includes.from(layout.projectDirectory.file("src/main/descriptions.md").toString())
            /*
            externalDocumentationLink {
                url.set(URL("https://javadoc.commandframework.cloud/")) //todo fix KDoc linking to JavaDoc
                packageListUrl.set(URL("https://javadoc.commandframework.cloud/allpackages-index.html"))
            }
             */
        }
    }
    javadocJar {
        from(dokkaHtml)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

kotlin {
    explicitApi()
}
