plugins {
    id("cloud.base-conventions")
    id("com.palantir.revapi")
}

dependencies {
    api(projects.cloudServices)
    compileOnly(libs.guice)
    testImplementation(libs.jmhCore)
    testImplementation(libs.jmhGeneratorAnnprocess)
    testImplementation(libs.guice)
}
