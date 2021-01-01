plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

dependencies {
    implementation("com.bmuschko", "gradle-nexus-plugin", "2.3.1")
}
