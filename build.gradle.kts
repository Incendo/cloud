plugins {
    id("cloud.parent-build-logic")
    id("com.github.ben-manes.versions")
}

subprojects {
    if (!name.startsWith("example-")) {
        apply(plugin = "cloud.publishing-conventions")
    }
}
