plugins {
    id("cloud.base-conventions")
    id("cloud.publishing-conventions")
    id("com.palantir.revapi")
}

dependencies {
    api(projects.cloudServices)
    compileOnly(libs.guice)
    testImplementation(libs.jmhCore)
    testImplementation(libs.jmhGeneratorAnnprocess)
    testImplementation(libs.guice)
}
