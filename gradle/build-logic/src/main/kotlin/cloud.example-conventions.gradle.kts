plugins {
    id("cloud.base-conventions")
}

val Project.compileExamples: Boolean
    get() = providers.gradleProperty("compile-examples")
        .isPresent

// Only compile examples on CI, or when the compile-examples property exists
if (!ci.get() && !compileExamples) {
    tasks.configureEach {
        onlyIf { false }
    }
}
