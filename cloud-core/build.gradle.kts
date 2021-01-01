dependencies {
    api(project(":cloud-services"))
    testImplementation("org.openjdk.jmh", "jmh-core", versions["jhm"])
    testImplementation("org.openjdk.jmh", "jmh-generator-annprocess", versions["jhm"])
}
