import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    kotlin("jvm") version "1.4.30"
    id("org.jetbrains.dokka") version "1.4.20"
}

tasks {
    withType<DokkaTask>().configureEach {
        dokkaSourceSets.getByName("main") {
            includes.from(layout.projectDirectory.file("src/main/descriptions.md").toString())
            externalDocumentationLink {
                url.set(URL("https://javadoc.commandframework.cloud/")) //todo fix KDoc linking to JavaDoc
                packageListUrl.set(URL("https://javadoc.commandframework.cloud/allpackages-index.html"))
            }
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

dependencies {
    api(project(":cloud-core"))
    testImplementation("org.jetbrains.kotlin", "kotlin-test-junit5")
}
