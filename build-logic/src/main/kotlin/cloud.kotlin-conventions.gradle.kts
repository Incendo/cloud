import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("cloud.base-conventions")
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).apply {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}

dependencies {
    api(kotlin("stdlib-jdk8"))
}

tasks {
    withType<DokkaTask> {
        dokkaSourceSets.named("main") {
            includes.from(layout.projectDirectory.file("src/main/descriptions.md"))
            /*externalDocumentationLink { // todo: fix KDoc linking to JavaDoc
                url.set(URL("https://javadoc.commandframework.cloud/"))
                packageListUrl.set(URL("https://javadoc.commandframework.cloud/allpackages-index.html"))
            }*/
        }
    }
    javadocJar {
        from(dokkaHtml)
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

spotless {
    kotlin {
        ktlint()
    }
}

kotlin {
    explicitApi()
}
