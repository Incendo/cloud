import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("cloud.base-conventions")
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

kotlin {
    explicitApi()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    coreLibrariesVersion = "1.5.31"
    target {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
                languageVersion = "1.5"
            }
        }
    }
}

dependencies {
    api(kotlin("stdlib-jdk8"))
}

tasks {
    withType(DokkaTask::class).configureEach {
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
}

spotless {
    kotlin {
        ktlint(libs.versions.ktlint.get())
            .editorConfigOverride(mapOf("ktlint_disabled_rules" to "filename"))
    }
}
