dependencies {
    api(project(":cloud-services"))
    compileOnly("com.google.inject", "guice", Versions.guice)
    testImplementation("org.openjdk.jmh", "jmh-core", Versions.jmh)
    testImplementation("org.openjdk.jmh", "jmh-generator-annprocess", Versions.jmh)
    testImplementation("com.google.inject", "guice", Versions.guice)
}
