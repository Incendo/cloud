plugins {
    id("cloud.base-conventions")
}

dependencies {
    api(projects.cloudServices)
    compileOnly(libs.guice)
    testImplementation(libs.jmhCore)
    testImplementation(libs.jmhGeneratorAnnprocess)
    testImplementation(libs.guice)
}
