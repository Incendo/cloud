plugins {
    id("cloud.base-conventions")
}

// Only compile examples on CI, or when the compile-examples property exists
if (!ci.get() && !compileExamples) {
    tasks.configureEach {
//        onlyIf { false }
    }
}
