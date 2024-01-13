import net.kyori.indra.IndraExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.incendo.cloudbuildlogic.city
import org.incendo.cloudbuildlogic.jmp

class CloudPublishingConventions : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("org.incendo.cloud-build-logic.publishing")

        target.extensions.configure(IndraExtension::class) {
            github("Incendo", "cloud") {
                ci(true)
            }
            mitLicense()

            configurePublications {
                pom {
                    developers {
                        city()
                        jmp()
                    }
                }
            }
        }
    }
}
