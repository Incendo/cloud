import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class CloudKotlinConventions : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("cloud.base-conventions")
        target.plugins.apply("org.jetbrains.kotlin.jvm")
        target.plugins.apply("org.jetbrains.dokka")

        val libs = target.libs

        target.extensions.configure(KotlinJvmProjectExtension::class) {
            explicitApi()
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }
            coreLibrariesVersion = libs.versions.kotlin.get()
            target {
                compilations.configureEach {
                    kotlinOptions {
                        jvmTarget = "1.8"
                        languageVersion = libs.versions.kotlin.get().split(".").take(2).joinToString(".")
                    }
                }
            }
        }

        target.dependencies {
            "api"(kotlin("stdlib-jdk8"))
        }

        target.tasks.withType(DokkaTask::class).configureEach {
            dokkaSourceSets.named("main") {
                includes.from(target.layout.projectDirectory.file("src/main/descriptions.md"))
                /*externalDocumentationLink { // todo: fix KDoc linking to JavaDoc
                    url.set(URL("https://javadoc.commandframework.cloud/"))
                    packageListUrl.set(URL("https://javadoc.commandframework.cloud/allpackages-index.html"))
                }*/
            }
        }
        target.tasks.named("javadocJar", AbstractArchiveTask::class) {
            from(target.tasks.named("dokkaHtml"))
        }
    }
}
