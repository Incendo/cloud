plugins {
    id("cloud.parent-build-logic")
    id("com.github.ben-manes.versions")
}

group = "cloud.commandframework"
version = "1.5.0-SNAPSHOT"
description = "Command framework and dispatcher for the JVM"

subprojects {
    plugins.apply("cloud.base-conventions")
}
