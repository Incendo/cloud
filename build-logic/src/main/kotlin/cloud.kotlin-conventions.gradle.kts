import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("cloud.base-conventions")
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).apply {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}

configurations.all {
    dependencies.removeIf { it.group == "org.jetbrains.kotlin" }
}

val compileAndTest: Configuration by configurations.creating
listOf(configurations.compileOnly, configurations.testImplementation).forEach { config ->
    config {
        extendsFrom(compileAndTest)
    }
}

dependencies {
    compileAndTest(kotlin("stdlib-jdk8"))
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

kotlin {
    explicitApi()
}
