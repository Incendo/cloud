import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.ltgt.errorprone")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.errorprone {
            /* These are just annoying */
            disable(
                "JdkObsolete",
                "FutureReturnValueIgnored",
                "ImmutableEnumChecker",
                "StringSplitter",
                "EqualsGetClass",
                "CatchAndPrintStackTrace",
                "InlineMeSuggester",
                "InlineTrivialConstant",
                "FunctionalInterfaceMethodChanged"
            )
            disableWarningsInGeneratedCode.set(true)
        }
    }
}
