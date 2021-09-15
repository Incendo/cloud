import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

configurations.all {
    dependencies.removeIf { it.group == "org.jetbrains.kotlin" }
}

dependencies {
    api(project(":cloud-core"))
    implementation(kotlin("stdlib-jdk8"))

    implementation(project(":cloud-annotations"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")

    testImplementation("org.jetbrains.kotlin", "kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
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
