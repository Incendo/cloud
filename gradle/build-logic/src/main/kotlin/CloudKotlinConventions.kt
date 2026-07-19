import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.named
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

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
            coreLibrariesVersion = libs.versions.kotlinLibrary.get()
            compilerOptions {
                languageVersion.set(KotlinVersion.KOTLIN_2_0)
                apiVersion.set(KotlinVersion.KOTLIN_2_0)
                jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }

        target.dependencies {
            "api"(libs.kotlinStdlibJdk8)
        }

        target.extensions.configure(DokkaExtension::class) {
            dokkaSourceSets.named("main") {
                includes.from(target.layout.projectDirectory.file("src/main/descriptions.md"))
                /*externalDocumentationLinks.register("cloud") { // todo: fix KDoc linking to JavaDoc
                    url("https://javadoc.commandframework.cloud/")
                    packageListUrl("https://javadoc.commandframework.cloud/allpackages-index.html")
                }*/
            }
        }

        val dokkaHtml = target.tasks.named(
            "dokkaGeneratePublicationHtml",
            DokkaGeneratePublicationTask::class,
        )
        target.tasks.named("javadocJar", AbstractArchiveTask::class) {
            from(dokkaHtml.flatMap { it.outputDirectory })
        }
    }
}
