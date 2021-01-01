dependencies {
    api(project(":cloud-services"))
    testImplementation("org.openjdk.jmh", "jmh-core", Versions.jhm)
    testImplementation("org.openjdk.jmh", "jmh-generator-annprocess", Versions.jhm)
}
