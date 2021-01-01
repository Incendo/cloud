dependencies {
    api(project(":cloud-services"))
    testImplementation("org.openjdk.jmh", "jmh-core", Versions.jmh)
    testImplementation("org.openjdk.jmh", "jmh-generator-annprocess", Versions.jmh)
}
