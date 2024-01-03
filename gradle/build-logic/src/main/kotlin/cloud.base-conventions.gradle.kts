plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("cloud.spotless-conventions")
    id("cloud.errorprone-conventions")
}

indra {
    javaVersions {
        minimumToolchain(17)
        target(8)
        testWith(8, 11, 17)
    }

    checkstyle().set(libs.versions.checkstyle)
}

/* Disable checkstyle on tests */
project.gradle.startParameter.excludedTaskNames.add("checkstyleTest")

tasks {
    withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:-processing,-classfile,-serial", "-Werror"))
    }
}

dependencies {
    compileOnlyApi(libs.bundles.annotations)

    compileOnly(libs.bundles.immutables)
    annotationProcessor(libs.bundles.immutables)

    testImplementation(libs.bundles.baseTestingDependencies)

    errorprone(libs.errorproneCore)
    // Silences compiler warnings from guava using errorprone
    compileOnly(libs.errorproneAnnotations)

    checkstyle(libs.stylecheck)
}
