dependencies {
    api(project(":cloud-services"))
    implementation("com.google.inject", "guice", Versions.guice)
    testImplementation("org.openjdk.jmh", "jmh-core", Versions.jmh)
    testImplementation("org.openjdk.jmh", "jmh-generator-annprocess", Versions.jmh)
}
