import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the

// set by GitHub Actions
val Project.ci: Provider<Boolean>
    get() = providers.environmentVariable("CI")
            .map { it.toBoolean() }
            .orElse(false)

val Project.compileExamples: Boolean
    get() = providers.gradleProperty("compile-examples")
            .isPresent

val Project.libs: LibrariesForLibs
    get() = the()
