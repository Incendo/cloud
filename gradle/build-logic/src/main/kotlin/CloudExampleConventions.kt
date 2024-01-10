import org.gradle.api.Plugin
import org.gradle.api.Project
import org.incendo.cloudbuildlogic.ciBuild

class CloudExampleConventions : Plugin<Project> {
    private val Project.compileExamples: Boolean
        get() = providers.gradleProperty("compile-examples").isPresent

    override fun apply(target: Project) {
        // Only compile examples on CI, or when the compile-examples property exists
        if (!target.providers.ciBuild.get() && !target.compileExamples) {
            target.tasks.configureEach {
                onlyIf { false }
            }
        }
    }
}
